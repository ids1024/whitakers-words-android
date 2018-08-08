package com.ids1024.whitakerswords

import android.text.TextUtils
import android.text.style.StyleSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Typeface
import android.graphics.Color
import android.text.SpannableStringBuilder

/**
* Parses plain text from `words` to add basic formatting (such as italics).
*/
fun parse_words(input: String): ArrayList<SpannableStringBuilder> {
    val results = ArrayList<SpannableStringBuilder>()

    var processed_result = SpannableStringBuilder()
    for (line in input.split("\n".toRegex())) {
        val words = line.split(" +".toRegex())
        var handled_line = TextUtils.join(" ", words)
        var pearse_code = 0
        if (words.size >= 1 && words[0].length == 2) {
            try {
                pearse_code = Integer.parseInt(words[0])
                handled_line = handled_line.substring(3)
            } catch (e: NumberFormatException) {
            }
        }
        // Indent meanings
        if (pearse_code == 3) {
            handled_line = "  $handled_line"
        }

        if (line.empty || line == "*") {
            if (line == "*") {
                processed_result.append("*")
            }
            val finalresult = processed_result.toString().trim()
            if (!finalresult.isEmpty()) {
                results.add(processed_result)
            }
            processed_result = SpannableStringBuilder()
            continue
        }

        val startindex = processed_result.length
        processed_result.append(handled_line + "\n")

        var span: Any? = null
        var endindex = processed_result.length
        when (pearse_code) {
        // Forms
            1 -> {
                span = StyleSpan(Typeface.BOLD)
                endindex = startindex + words[1].length
            }
        // Dictionary forms
            2 -> {
                // A HACK(?) for parsing output of searches like
                // "quod", which show shorter output for dictionary forms
                if (!words[1].startsWith("[")) {
                    var index = 1
                    endindex = startindex
                    do {
                        endindex += words[index].length + 1
                        index += 1
                    } while (words[index - 1].endsWith(","))

                    span = StyleSpan(Typeface.BOLD)
                }
            }
        // Meaning
            3 -> span = StyleSpan(Typeface.ITALIC)
        // Not found
            4 -> span = ForegroundColorSpan(Color.RED)
        // Addons
            5 -> {
            }
        // Tricks/syncope/addons?
            6 -> {
            }
        }
        processed_result.setSpan(span, startindex, endindex, 0)
    }
    val finalresult = processed_result.toString().trim()
    if (!finalresult.isEmpty()) {
        results.add(processed_result)
    }

    return results
}
