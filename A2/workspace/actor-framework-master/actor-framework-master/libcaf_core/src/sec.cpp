/******************************************************************************
 *                       ____    _    _____                                   *
 *                      / ___|  / \  |  ___|    C++                           *
 *                     | |     / _ \ | |_       Actor                         *
 *                     | |___ / ___ \|  _|      Framework                     *
 *                      \____/_/   \_|_|                                      *
 *                                                                            *
 * Copyright 2011-2018 Dominik Charousset                                     *
 *                                                                            *
 * Distributed under the terms and conditions of the BSD 3-Clause License or  *
 * (at your option) under the terms and conditions of the Boost Software      *
 * License 1.0. See accompanying files LICENSE and LICENSE_ALTERNATIVE.       *
 *                                                                            *
 * If you did not receive a copy of the license files, see                    *
 * http://opensource.org/licenses/BSD-3-Clause and                            *
 * http://www.boost.org/LICENSE_1_0.txt.                                      *
 ******************************************************************************/

#include "caf/sec.hpp"

#include "caf/error.hpp"
#include "caf/make_message.hpp"
#include "caf/message.hpp"

namespace caf {

error make_error(sec x) {
  return {static_cast<uint8_t>(x), atom("system")};
}

error make_error(sec x, message msg) {
  return {static_cast<uint8_t>(x), atom("system"), std::move(msg)};
}

error make_error(sec x, std::string msg) {
  return make_error(x, make_message(std::move(msg)));
}

} // namespace caf
