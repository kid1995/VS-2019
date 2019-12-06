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

#include "caf/detail/type_list.hpp"
#include "caf/fwd.hpp"
#include "caf/replies_to.hpp"

namespace caf {

/// Computes the type for f*g (actor composition).
///
/// This metaprogramming function implements the following pseudo-code (with
/// `f` and `g` modelled as pairs where the first element is the input type and
/// the second type is the output type).
///
/// ~~~
/// let composed_type f g =
///   [(fst x, snd y) | x <- g, y <- f,
///                     snd x == fst y]
/// ~~~
///
/// This class implements the list comprehension above in a single shot with
/// worst case n*m template instantiations using an inner and outer loop, where
/// n is the size of `Xs` and m the size of `Ys`. `Zs` is a helper that models
/// the "inner loop variable" for generating the cross product of `Xs` and
/// `Ys`. `Rs` collects the results.
template <class Xs, class Ys, class Zs, class Rs>
struct composed_type;

// End of outer loop over Xs.
template <class Ys, class Zs, class... Rs>
struct composed_type<detail::type_list<>, Ys, Zs, detail::type_list<Rs...>> {
  using type = typed_actor<Rs...>;
};

// End of inner loop Ys (Zs).
template <class X, class... Xs, class Ys, class Rs>
struct composed_type<detail::type_list<X, Xs...>, Ys, detail::type_list<>, Rs>
  : composed_type<detail::type_list<Xs...>, Ys, Ys, Rs> {};

// Output type matches the input type of the next actor.
template <class... In, class... Out, class... Xs, class Ys, class... MapsTo,
          class... Zs, class... Rs>
struct composed_type<
  detail::type_list<typed_mpi<detail::type_list<In...>, output_tuple<Out...>>,
                    Xs...>,
  Ys,
  detail::type_list<
    typed_mpi<detail::type_list<Out...>, output_tuple<MapsTo...>>, Zs...>,
  detail::type_list<Rs...>>
  : composed_type<
      detail::type_list<Xs...>, Ys, Ys,
      detail::type_list<
        Rs..., typed_mpi<detail::type_list<In...>, output_tuple<MapsTo...>>>> {
};

// No match, recurse over Zs.
template <class In, class Out, class... Xs, class Ys, class Unrelated,
          class MapsTo, class... Zs, class Rs>
struct composed_type<detail::type_list<typed_mpi<In, Out>, Xs...>, Ys,
                     detail::type_list<typed_mpi<Unrelated, MapsTo>, Zs...>, Rs>
  : composed_type<detail::type_list<typed_mpi<In, Out>, Xs...>, Ys,
                  detail::type_list<Zs...>, Rs> {};

/// Convenience type alias.
/// @relates composed_type
template <class F, class G>
using composed_type_t = typename composed_type<G, F, F,
                                               detail::type_list<>>::type;

} // namespace caf
