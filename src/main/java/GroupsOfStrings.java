import java.io.*;
import java.util.*;
import java.util.List;

public class GroupsOfStrings {
    public static List<String[]> read_strings(String file_name) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file_name));
        List<String> strings = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            strings.add(line);
        }
        return format_data(strings);
    }

    public static boolean check_string(String[] strs) {
        if (strs.length == 0)
            return false;
        for (String s : strs) {
            String[] tmp = s.split("\"");
            if (tmp.length > 2)
                return false;
        }
        return true;
    }

    public static List<String[]> format_data(List<String> strings) {
        List<String[]> res = new ArrayList<>(strings.size());
        for (String s : strings) {
            String[] elem = s.split(";");
            if (check_string(elem))
                res.add(elem);
        }
        return res;
    }

    public static int max_length(List<String[]> data) {
        int max = 0;
        for (String[] strings : data) {
            if (strings.length > max)
                max = strings.length;
        }
        return max;
    }

    public static void change_group(int[] strings, HashSet<Integer> strings_index, int group) {
        for (int str : strings_index) {
            strings[str] = group;
        }
    }

    public static HashMap<Integer, HashSet<Integer>> grouping(List<String[]> lines, int max_line) {
        HashMap<Integer, HashSet<Integer>> groups = new HashMap<>();
        int[] strings_with_group = new int[lines.size()];
        HashMap<String, Integer> tmp = new HashMap<>();

        for (int i = 0; i < strings_with_group.length; i++)
            strings_with_group[i] = i;

        for (int i = 0; i < max_line; i++) {
            for (int j = 0; j < lines.size(); j++)  {
                if (lines.get(j).length > i && !(lines.get(j)[i].equals("\"\"") || lines.get(j)[i].equals(""))) {
                    if (tmp.containsKey(lines.get(j)[i])) {
                        int str_num = tmp.get(lines.get(j)[i]);
                        int target_group = strings_with_group[str_num];
                        int cur_group = strings_with_group[j];
                        if (cur_group != target_group) {
                            HashSet<Integer> target_set = groups.get(target_group);
                            HashSet<Integer> cur_set = groups.get(cur_group);
                            if (target_set == null) {
                                target_set = new HashSet<>();
                                target_set.add(str_num);
                            }
                            if (cur_set == null) {
                                cur_set = new HashSet<>();
                                cur_set.add(j);
                            }
                            target_set.addAll(cur_set);
                            groups.put(target_group, target_set);
                            groups.remove(cur_group);
                            change_group(strings_with_group, cur_set, target_group); //change in strings
                        }
                    } else {
                        tmp.put(lines.get(j)[i], j);
                    }
                }
            }
            tmp.clear();
        }
        return groups;
    }

    public static void strings_without_groups(HashMap<Integer, HashSet<Integer>> groups, boolean[] res) {
        for (int key : groups.keySet()) {
            for (int group : groups.get(key))
                res[group] = true;
        }
    }

    public static List<String> make_full_lines(List<String[]> strings) {
        List<String> full_lines = new ArrayList<>(strings.size());

        for (String[] str : strings) {
            StringBuilder tmp = new StringBuilder();
            for (int i = 0; i < str.length - 1; i++) {
                tmp.append(str[i]);
                tmp.append(";");
            }
            tmp.append(str[str.length - 1]);
            full_lines.add(tmp.toString());
        }

        return full_lines;
    }

    public static void remove_duplicates(HashMap<Integer, HashSet<Integer>> groups, List<String> full_lines) {
        for (int key : groups.keySet()) {
            if (groups.get(key).size() > 1) {
                HashMap<String, Integer> unic_strings = new HashMap<>();
                for (int str_index : groups.get(key)) {
                    unic_strings.put(full_lines.get(str_index), str_index);
                }
                HashSet<Integer> set = new HashSet<>(unic_strings.values());
                groups.put(key, set);
            }
        }
    }

    public static int count_groups(HashMap<Integer, HashSet<Integer>> groups) {
        int count = 0;
        for (int key : groups.keySet()) {
            if (groups.get(key).size() > 1)
                count += 1;
        }
        return count;
    }

    public static void write_groups(String file_name, List<HashSet<Integer>> groups, List<String> lines, int num) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file_name));
        writer.write(num + "\n");
        int count = 1;
        for (HashSet<Integer> set : groups) {
            writer.write("Group " + count + "\n");
            for (int str : set) {
                writer.write(lines.get(str));
                writer.write("\n");
            }
            writer.write("\n");
            count++;
        }
        writer.close();
    }

    public static void write_single_strings(String file_name, boolean[] in_groups, List<String> lines, int count) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file_name, true));
        for (int i = 0; i < in_groups.length; i++) {
            if (!in_groups[i]) {
                writer.write("Group " + count + "\n");
                writer.write(lines.get(i));
                writer.write("\n\n");
                count++;
            }
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
//        long startTime = System.nanoTime();

        String in = args[0];
        List<String[]> strings = read_strings(in);

        int max_len = max_length(strings);

        HashMap<Integer, HashSet<Integer>> groups = grouping(strings, max_len);

        boolean[] strings_in_groups = new boolean[strings.size()];
        strings_without_groups(groups, strings_in_groups);

        List<String> full_lines = make_full_lines(strings);

        remove_duplicates(groups, full_lines);

        int count = count_groups(groups);

        List<HashSet<Integer>> values = new ArrayList<>(groups.values());
        Comparator<HashSet<Integer>> groupsComparator = Comparator.comparing(set -> set.size());
        Comparator<HashSet<Integer>> groupsComparatorReverse = groupsComparator.reversed();
        values.sort(groupsComparatorReverse);

        String out = "out.txt";
        write_groups(out, values, full_lines, count);
        write_single_strings(out, strings_in_groups, full_lines, count + 1);

//        long endTime = System.nanoTime();
//        System.out.println("Total: " + (endTime - startTime) / 1000000000.0);
    }
}
