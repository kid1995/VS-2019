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

#pragma once

#include <cstddef>
#include <cstdint>

#include "caf/actor_clock.hpp"
#include "caf/actor_control_block.hpp"
#include "caf/credit_controller.hpp"
#include "caf/downstream_msg.hpp"
#include "caf/meta/type_name.hpp"
#include "caf/rtti_pair.hpp"
#include "caf/stream_aborter.hpp"
#include "caf/stream_manager.hpp"
#include "caf/stream_priority.hpp"
#include "caf/stream_slot.hpp"
#include "caf/timestamp.hpp"
#include "caf/upstream_msg.hpp"

namespace caf {

/// State for a path to an upstream actor (source).
class inbound_path {
public:
  /// Message type for propagating graceful shutdowns.
  using regular_shutdown = upstream_msg::drop;

  /// Message type for propagating errors.
  using irregular_shutdown = upstream_msg::forced_drop;

  /// Points to the manager responsible for incoming traffic.
  stream_manager_ptr mgr;

  /// Handle to the source.
  strong_actor_ptr hdl;

  /// Stores slot IDs for sender (hdl) and receiver (self).
  stream_slots slots;

  /// Stores the last computed desired batch size.
  int32_t desired_batch_size = 0;

  /// Amount of credit we have signaled upstream.
  int32_t assigned_credit = 0;

  /// Priority of incoming batches from this source.
  stream_priority prio = stream_priority::normal;

  /// ID of the last acknowledged batch ID.
  int64_t last_acked_batch_id = 0;

  /// ID of the last received batch.
  int64_t last_batch_id = 0;

  /// Controller for assigning credit to the source.
  std::unique_ptr<credit_controller> controller_;

  /// Stores the time point of the last credit decision for this source.
  actor_clock::time_point last_credit_decision;

  /// Stores the time point of the last credit decision for this source.
  actor_clock::time_point next_credit_decision;

  /// Constructs a path for given handle and stream ID.
  inbound_path(stream_manager_ptr mgr_ptr, stream_slots id,
               strong_actor_ptr ptr, rtti_pair input_type);

  ~inbound_path();

  /// Updates `last_batch_id` and `assigned_credit` before dispatching to the
  /// manager.
  void handle(downstream_msg::batch& x);

  /// Dispatches any `downstream_msg` other than `batch` directly to the
  /// manager.
  template <class T>
  void handle(T& x) {
    mgr->handle(this, x);
  }

  /// Emits an `upstream_msg::ack_batch`.
  void emit_ack_open(local_actor* self, actor_addr rebind_from);

  /// Sends an `upstream_msg::ack_batch` for granting new credit. Credit is
  /// calculated from sampled batch durations, the cycle duration and the
  /// desired batch complexity.
  /// @param self Points to the parent actor, i.e., sender of the message.
  /// @param queued_items Accumulated size of all batches that are currently
  ///                     waiting in the mailbox.
  /// @param now Current timestamp.
  /// @param cycle Time between credit rounds.
  void emit_ack_batch(local_actor* self, int32_t queued_items,
                      actor_clock::time_point now, timespan cycle);

  /// Returns whether the path received no input since last emitting
  /// `ack_batch`, i.e., `last_acked_batch_id == last_batch_id`.
  bool up_to_date();

  /// Sends an `upstream_msg::drop` on this path.
  void emit_regular_shutdown(local_actor* self);

  /// Sends an `upstream_msg::forced_drop` on this path.
  void emit_irregular_shutdown(local_actor* self, error reason);

  /// Sends an `upstream_msg::forced_drop`.
  static void emit_irregular_shutdown(local_actor* self, stream_slots slots,
                                      const strong_actor_ptr& hdl,
                                      error reason);

  /// Returns a pointer to the parent actor.
  scheduled_actor* self();
};

/// @relates inbound_path
template <class Inspector>
typename Inspector::return_type inspect(Inspector& f, inbound_path& x) {
  return f(meta::type_name("inbound_path"), x.hdl, x.slots, x.prio,
           x.last_acked_batch_id, x.last_batch_id, x.assigned_credit);
}

} // namespace caf
