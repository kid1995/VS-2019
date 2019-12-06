#include <algorithm>
#include <cstring>
#include <fstream>
#include <iostream>
#include <string>
#include <vector>

using std::cerr;
using std::find;
using std::find_if;
using std::string;
using std::vector;

void split(const string& str, const string& separator, vector<string>& dest) {
  size_t current, previous = 0;
  current = str.find(separator);
  while (current != std::string::npos) {
    dest.emplace_back(str.substr(previous, current - previous));
    previous = current + separator.size();
    current = str.find(separator, previous);
  }
  dest.push_back(str.substr(previous, current - previous));
}

void trim(string& str) {
  auto not_space = [](char c) { return isspace(c) == 0; };
  str.erase(str.begin(), find_if(str.begin(), str.end(), not_space));
  str.erase(find_if(str.rbegin(), str.rend(), not_space).base(), str.end());
}

template <size_t N>
bool starts_with(const string& str, const char (&prefix)[N]) {
  return str.compare(0, N - 1, prefix) == 0;
}

template <size_t N>
void drop_prefix(string& str, const char (&prefix)[N]) {
  if (str.compare(0, N - 1, prefix) == 0)
    str.erase(str.begin(), str.begin() + (N - 1));
}

void keep_alnum(string& str) {
  auto not_alnum = [](char c) { return isalnum(c) == 0 && c != '_'; };
  str.erase(find_if(str.begin(), str.end(), not_alnum), str.end());
}

int main(int argc, char** argv) {
  if (argc != 3) {
    cerr << "wrong number of arguments.\n"
         << "usage: " << argv[0] << "input-file output-file\n";
    return EXIT_FAILURE;
  }
  std::ifstream in{argv[1]};
  if (!in) {
    cerr << "unable to open input file: " << argv[1] << '\n';
    return EXIT_FAILURE;
  }
  vector<string> namespaces;
  string enum_name;
  string line;
  bool is_enum_class = false;
  // Locate the beginning of the enum.
  for (;;) {
    if (!getline(in, line)) {
      cerr << "unable to locate enum in file: " << argv[1] << '\n';
      return EXIT_FAILURE;
    }
    trim(line);
    if (starts_with(line, "enum ")) {
      drop_prefix(line, "enum ");
      if (starts_with(line, "class ")) {
        is_enum_class = true;
        drop_prefix(line, "class ");
      }
      trim(line);
      keep_alnum(line);
      enum_name = line;
      break;
    }
    if (starts_with(line, "namespace ")) {
      if (line.back() == '{')
        line.pop_back();
      line.erase(line.begin(), find(line.begin(), line.end(), ' '));
      trim(line);
      split(line, "::", namespaces);
    }
  }
  // Sanity checking.
  if (namespaces.empty()) {
    cerr << "enum found outside of a namespace\n";
    return EXIT_FAILURE;
  }
  if (enum_name.empty()) {
    cerr << "empty enum name found\n";
    return EXIT_FAILURE;
  }
  std::ofstream out{argv[2]};
  if (!out) {
    cerr << "unable to open output file: " << argv[1] << '\n';
    return EXIT_FAILURE;
  }
  // Print file header.
  out << "#include \"" << namespaces[0];
  for (size_t i = 1; i < namespaces.size(); ++i)
    out << '/' << namespaces[i];
  out << '/' << enum_name << ".hpp\"\n\n"
      << "#include <string>\n\n"
      << "namespace " << namespaces[0] << " {\n";
  for (size_t i = 1; i < namespaces.size(); ++i)
    out << "namespace " << namespaces[i] << " {\n";
  out << "\nstd::string to_string(" << enum_name << " x) {\n"
      << "  switch(x) {\n"
      << "    default:\n"
      << "      return \"???\";\n";

  // Read until hitting the closing '}'.
  std::string case_label_prefix;
  if (is_enum_class)
    case_label_prefix = enum_name + "::";
  for (;;) {
    if (!getline(in, line)) {
      cerr << "unable to read enum values\n";
      return EXIT_FAILURE;
    }
    trim(line);
    if (line.empty())
      continue;
    if (line[0] == '}')
      break;
    if (line[0] != '/') {
      keep_alnum(line);
      out << "    case " << case_label_prefix << line << ":\n"
          << "      return \"" << line << "\";\n";
    }
  }
  // Done. Print file footer and exit.
  out << "  };\n"
      << "}\n\n";
  for (auto i = namespaces.rbegin(); i != namespaces.rend(); ++i)
    out << "} // namespace " << *i << '\n';
}
