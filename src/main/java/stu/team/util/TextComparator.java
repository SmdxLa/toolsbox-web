package stu.team.util;

import java.util.Arrays;
import java.util.List;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

public class TextComparator {

    public static String compare(String original, String compared) {
        List<String> originalLines = Arrays.asList(original.split("\n"));
        List<String> comparedLines = Arrays.asList(compared.split("\n"));

        Patch<String> patch = DiffUtils.diff(originalLines, comparedLines);
        List<AbstractDelta<String>> deltas = patch.getDeltas();

        StringBuilder diffResult = new StringBuilder();
        for (AbstractDelta<String> delta : deltas) {
            int startOriginal = delta.getSource().getPosition();
            int startCompared = delta.getTarget().getPosition();

            diffResult.append("从行 ")
                    .append(startOriginal + 1)
                    .append(" 到行 ")
                    .append(startCompared + 1)
                    .append(" 的差异：\n")
                    .append("原文：\n");

            String originalLine = originalLines.get(startOriginal);
            String comparedLine = comparedLines.get(startCompared);
            diffResult.append(markDifferences(originalLine, comparedLine))
                    .append("\n\n修改为：\n")
                    .append("对比文本：\n")
                    .append(markDifferences(comparedLine, originalLine))
                    .append("\n\n");
        }

        return diffResult.toString();
    }


    private static String markDifferences(String line1, String line2) {
        String[] words1 = line1.split("\\s+");
        String[] words2 = line2.split("\\s+");
        StringBuilder marked = new StringBuilder();

        for (int i = 0; i < words1.length; i++) {
            if (i < words2.length && !words1[i].equals(words2[i])) {
                marked.append('[').append(words1[i]).append(']');
            } else if (i < words2.length) {
                marked.append(words1[i]);
            }
            marked.append(' ');
        }

        return marked.toString().trim();
    }
}
