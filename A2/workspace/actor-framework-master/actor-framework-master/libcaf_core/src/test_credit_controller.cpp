/******************************************************************************
 *                       ____    _    _____                                   *
 *                      / ___|  / \  |  ___|    C++                           *
 *                     | |     / _ \ | |_       Actor                         *
 *                     | |___ / ___ \|  _|      Framework                     *
 *                      \____/_/   \_|_|                                      *
 *                                                                            *
 * Copyright 2011-2019 Dominik Charousset                                     *
 *                                                                            *
 * Distributed under the terms and conditions of the BSD 3-Clause License or  *
 * (at your option) under the terms and conditions of the Boost Software      *
 * License 1.0. See accompanying files LICENSE and LICENSE_ALTERNATIVE.       *
 *                                                                            *
 * If you did not receive a copy of the license files, see                    *
 * http://opensource.org/licenses/BSD-3-Clause and                            *
 * http://www.boost.org/LICENSE_1_0.txt.                                      *
 ******************************************************************************/

#include "caf/detail/test_credit_controller.hpp"

#include "caf/actor_system.hpp"
#include "caf/actor_system_config.hpp"
#include "caf/scheduled_actor.hpp"

namespace caf {
namespace detail {

test_credit_controller::~test_credit_controller() {
  // nop
}

void test_credit_controller::before_processing(downstream_msg::batch& x) {
  num_elements_ += x.xs_size;
}

void test_credit_controller::after_processing(downstream_msg::batch&) {
  // nop
}

credit_controller::assignment test_credit_controller::compute_initial() {
  return {50, 50};
}

credit_controller::assignment
test_credit_controller::compute(timespan cycle, int32_t) {
  auto& cfg = self()->system().config();
  auto complexity = cfg.stream_desired_batch_complexity;
  // Max throughput = C * (N / t), where C = cycle length, N = measured items,
  // and t = measured time. Desired batch size is the same formula with D
  // (desired complexity) instead of C. We compute our values in 64-bit for
  // more precision before truncating to a 32-bit integer type at the end.
  int64_t total_ns = num_elements_ * 1000; // calculate with 1us per element
  if (total_ns == 0)
    return {1, 1};
  // Helper for truncating a 64-bit integer to a 32-bit integer with a minimum
  // value of 1.
  auto clamp = [](int64_t x) -> int32_t {
    static constexpr auto upper_bound = std::numeric_limits<int32_t>::max();
    if (x > upper_bound)
      return upper_bound;
    if (x <= 0)
      return 1;
    return static_cast<int32_t>(x);
  };
  // Instead of C * (N / t) we calculate (C * N) / t to avoid double conversion
  // and rounding errors.
  assignment result;
  result.credit = clamp((cycle.count() * num_elements_) / total_ns);
  result.batch_size = clamp((complexity.count() * num_elements_) / total_ns);
  // Reset state and return.
  num_elements_ = 0;
  return result;
}

} // namespace detail
} // namespace caf
