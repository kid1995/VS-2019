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

#define CAF_SUITE detail.parser.read_bool

#include "caf/detail/parser/read_bool.hpp"

#include "caf/test/unit_test.hpp"

#include <string>

#include "caf/parser_state.hpp"
#include "caf/string_view.hpp"
#include "caf/variant.hpp"

using namespace caf;

namespace {

struct bool_parser_consumer {
  bool x;
  inline void value(bool y) {
    x = y;
  }
};

using res_t = variant<pec, bool>;

struct bool_parser {
  res_t operator()(string_view str) {
    bool_parser_consumer f;
    string_parser_state res{str.begin(), str.end()};
    detail::parser::read_bool(res, f);
    if (res.code == pec::success)
      return f.x;
    return res.code;
  }
};

struct fixture {
  bool_parser p;
};

} // namespace

CAF_TEST_FIXTURE_SCOPE(read_bool_tests, fixture)

CAF_TEST(valid booleans) {
  CAF_CHECK_EQUAL(p("true"), true);
  CAF_CHECK_EQUAL(p("false"), false);
}

CAF_TEST(invalid booleans) {
  CAF_CHECK_EQUAL(p(""), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("t"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("tr"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("tru"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p(" true"), pec::unexpected_character);
  CAF_CHECK_EQUAL(p("f"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("fa"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("fal"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("fals"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p(" false"), pec::unexpected_character);
  CAF_CHECK_EQUAL(p("tr\nue"), pec::unexpected_newline);
  CAF_CHECK_EQUAL(p("trues"), pec::trailing_character);
}

CAF_TEST_FIXTURE_SCOPE_END()
