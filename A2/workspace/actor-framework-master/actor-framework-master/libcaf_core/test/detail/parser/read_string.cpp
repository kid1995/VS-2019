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

#define CAF_SUITE detail.parser.read_string

#include "caf/detail/parser/read_string.hpp"

#include "caf/test/unit_test.hpp"

#include <string>

#include "caf/parser_state.hpp"
#include "caf/string_view.hpp"
#include "caf/variant.hpp"

using namespace caf;

namespace {

struct string_parser_consumer {
  std::string x;
  inline void value(std::string y) {
    x = std::move(y);
  }
};

using res_t = variant<pec, std::string>;

struct string_parser {
  res_t operator()(string_view str) {
    string_parser_consumer f;
    string_parser_state res{str.begin(), str.end()};
    detail::parser::read_string(res, f);
    if (res.code == pec::success)
      return f.x;
    return res.code;
  }
};

struct fixture {
  string_parser p;
};

// TODO: remove and use "..."s from the STL when switching to C++14
std::string operator"" _s(const char* str, size_t str_len) {
  std::string result;
  result.assign(str, str_len);
  return result;
}

} // namespace

CAF_TEST_FIXTURE_SCOPE(read_string_tests, fixture)

CAF_TEST(empty string) {
  CAF_CHECK_EQUAL(p(R"("")"), ""_s);
  CAF_CHECK_EQUAL(p(R"( "")"), ""_s);
  CAF_CHECK_EQUAL(p(R"(  "")"), ""_s);
  CAF_CHECK_EQUAL(p(R"("" )"), ""_s);
  CAF_CHECK_EQUAL(p(R"(""  )"), ""_s);
  CAF_CHECK_EQUAL(p(R"(  ""  )"), ""_s);
  CAF_CHECK_EQUAL(p("\t \"\" \t\t\t "), ""_s);
}

CAF_TEST(non - empty quoted string) {
  CAF_CHECK_EQUAL(p(R"("abc")"), "abc"_s);
  CAF_CHECK_EQUAL(p(R"("a b c")"), "a b c"_s);
  CAF_CHECK_EQUAL(p(R"(   "abcdefABCDEF"   )"), "abcdefABCDEF"_s);
}

CAF_TEST(quoted string with escaped characters) {
  CAF_CHECK_EQUAL(p(R"("a\tb\tc")"), "a\tb\tc"_s);
  CAF_CHECK_EQUAL(p(R"("a\nb\r\nc")"), "a\nb\r\nc"_s);
  CAF_CHECK_EQUAL(p(R"("a\\b")"), "a\\b"_s);
}

CAF_TEST(unquoted strings) {
  CAF_CHECK_EQUAL(p(R"(foo)"), "foo"_s);
  CAF_CHECK_EQUAL(p(R"( foo )"), "foo"_s);
  CAF_CHECK_EQUAL(p(R"( 123 )"), "123"_s);
}

CAF_TEST(invalid strings) {
  CAF_CHECK_EQUAL(p(R"("abc)"), pec::unexpected_eof);
  CAF_CHECK_EQUAL(p("\"ab\nc\""), pec::unexpected_newline);
  CAF_CHECK_EQUAL(p(R"("abc" def)"), pec::trailing_character);
  CAF_CHECK_EQUAL(p(R"( 123, )"), pec::trailing_character);
}

CAF_TEST_FIXTURE_SCOPE_END()
