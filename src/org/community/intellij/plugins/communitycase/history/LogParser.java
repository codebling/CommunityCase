/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.history;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;

import java.util.*;

/**
 * <p>Parses the ' log' output basing on the given number of options.
 * Doesn't execute of prepare the command itself, performs only parsing.</p>
 *
 * <p>
 * Usage:
 * 1. Pass options you want to have in the output to the constructor using the {@link LogOption} enum constants.
 * 2. Get the custom format pattern for ' log' by calling {@link #getFormatOption()}
 * 3. Call the command and retrieve the output.
 * 4. Parse the output via {@link #parse(String)} or {@link #parseOneRecord(String)} (if you want the output to be parsed line by line).</p>
 *
 * <p>The class is package visible, since it's used only in HistoryUtils - the class which retrieve various pieced of history information
 * in different formats from ' log'</p>
 *
 * <p>Note that you may pass one set of options to the LogParser constructor and then execute log with other set of options.
 * In that case {@link #parse(String)} will parse only those options which you've specified in the constructor.
 * Others will be ignored since the parser knows nothing about them: it just gets the ' log' output to parse.
 * Moreover you really <b>must</b> use {@link #getFormatOption()} to pass "--pretty=format" pattern to ' log' - otherwise the parser won't be able
 * to parse output of ' log' (because special separator characters are used for that).</p>
 *
 * <p>If you use '--name-status' or '--name-only' flags in ' log' you also <b>must</b> call {@link #parseStatusBeforeName(boolean)} with
 * true or false respectively, because it also affects the output.</p>
 *  
 * @see org.community.intellij.plugins.communitycase.history.LogRecord
 */
class LogParser {
  // Single records begin with %x01, end with %03. Items of commit information (hash, committer, subject, etc.) are separated by %x02.
  // each character is declared twice - for pattern format and for actual character in the output.
  // separators are declared as String instead of char, because String#split() is heavily used in parsing.
  public static final String RECORD_START = "\u0001";
  public static final String RECORD_START_ = "\\001";
  private static final String ITEMS_SEPARATOR = "\u0002";
  private static final String ITEMS_SEPARATOR_ = "\\002";
  private static final String RECORD_END = "\u0003";
  private static final String RECORD_END_ = "\\003";

  private final String myFormat;  // pretty custom format generated in the constructor
  private final LogOption[] myOptions;

  private enum NameStatus { NONE, NAME, STATUS } // --name-only, --name-status or no flag
  private NameStatus myNameStatusOutputted = NameStatus.NONE;

  /**
   * Options which may be passed to ' log --pretty=format:' as placeholders and then parsed from the result.
   * These are the pieces of information about a commit which we want to get from ' log'.
   */
  enum LogOption {
    ELEMENT_NAME("En"),VERSION("Vn"),TIME("Nd"),ACTION_NAME("o"),ACTION_DESC("e"),ACTION_NAME_DESC("o/%e"),USER("u"),COMMENT("Nc");
    /*
    SHORT_HASH("h"), HASH("H"), COMMIT_TIME("ct"), AUTHOR_NAME("an"), AUTHOR_TIME("at"), AUTHOR_EMAIL("ae"), COMMITTER_NAME("cn"), COMMITTER_EMAIL("ce"), SUBJECT("s"), BODY("b"),
    SHORT_PARENTS("p"), PARENTS("P"), REF_NAMES("d");
    */
    /*
           ·    %H: commit hash
           ·    %h: abbreviated commit hash
           ·    %T: tree hash
           ·    %t: abbreviated tree hash
           ·    %P: parent hashes
           ·    %p: abbreviated parent hashes
           ·    %an: author name
           ·    %aN: author name (respecting .mailmap, see git-shortlog(1) or
               git-blame(1))
           ·    %ae: author email
           ·    %aE: author email (respecting .mailmap, see git-shortlog(1) or
               git-blame(1))
           ·    %ad: author date (format respects --date= option)
           ·    %aD: author date, RFC2822 style
           ·    %ar: author date, relative
           ·    %at: author date, UNIX timestamp
           ·    %ai: author date, ISO 8601 format
           ·    %cn: committer name
           ·    %cN: committer name (respecting .mailmap, see git-shortlog(1)
               or git-blame(1))
           ·    %ce: committer email
           ·    %cE: committer email (respecting .mailmap, see git-shortlog(1)
               or git-blame(1))
           ·    %cd: committer date
           ·    %cD: committer date, RFC2822 style
           ·    %cr: committer date, relative
           ·    %ct: committer date, UNIX timestamp
           ·    %ci: committer date, ISO 8601 format
           ·    %d: ref names, like the --decorate option of git-log(1)
           ·    %e: encoding
           ·    %s: subject
           ·    %f: sanitized subject line, suitable for a filename
           ·    %b: body
           ·    %B: raw body (unwrapped subject and body)
           ·    %N: commit notes
           ·    %gD: reflog selector, e.g., refs/stash@{1}
           ·    %gd: shortened reflog selector, e.g., stash@{1}
           ·    %gs: reflog subject
           ·    %Cred: switch color to red
           ·    %Cgreen: switch color to green
           ·    %Cblue: switch color to blue
           ·    %Creset: reset color
           ·    %C(...): color specification, as described in color.branch.*
               config option
           ·    %m: left, right or boundary mark
           ·    %n: newline
           ·    %%: a raw %
           ·    %x00: print a byte from a hex code
           ·    %w([<w>[,<i1>[,<i2>]]]): switch line wrapping, like the -w
               option of git-shortlog(1).
     */

    private String myPlaceholder;
    private LogOption(String placeholder) { myPlaceholder = placeholder; }
    private String getPlaceholder() { return myPlaceholder; }
  }

  /**
   * Constructs new parser with the specified number of options. Only these options will be parsed out and thus will be available from
   * LogRecord.
   */
  LogParser(LogOption... options) {
    Function<LogOption,String> function = new Function<LogOption, String>() {
      @Override
      public String fun(LogOption option) {
        return "%" + option.getPlaceholder();
      }
    };
    myFormat = RECORD_START_ + StringUtil.join(options, function, ITEMS_SEPARATOR_) + RECORD_END_;
    myOptions = options;
  }

  String getFormatOption() {
    return "-fmt \"" + myFormat + "\\n%" + LogOption.ELEMENT_NAME.name() + "\\n\\n" + "\"";
  }

  /**
   * Call this method to indicate that " log" is called with --name-only or --name-status flag.
   * (Note that these flags are mutually exclusive).
   * The LogParser will parse the output concerning that output contains path or status and path.
   * @param nameStatus true if --name-status is passed, false if --name-only is passed.
   */
  void parseStatusBeforeName(boolean nameStatus) {
    myNameStatusOutputted = nameStatus ? NameStatus.STATUS : NameStatus.NAME;
  }

  /**
   * Parses the output returned from ' log' which was executed with '--pretty=format:' pattern retrieved from {@link #getFormatOption()}.
   * @param output ' log' output to be parsed.
   * @return The list of LogRecords with information for each revision. The list is sorted as usual for log - the first is the newest,
   * the last is the oldest.
   */
  List<LogRecord> parse(String output) {
    // Here is what log returns for --pretty=tformat:^%H#%s$
    // ^2c815939f45fbcfda9583f84b14fe9d393ada790#sample commit$
    //
    // D       a.txt
    // ^b71477e9738168aa67a8d41c414f284255f81e8a#moved out$
    //
    // R100    dir/anew.txt    anew.txt
    final String[] records = output.split(RECORD_START); // split by START, because END is the end of information, but not the end of the record: file status and path follow.
    final List<LogRecord> res = new ArrayList<LogRecord>(records.length);
    for (String record : records) {
      if (!record.trim().isEmpty()) {  // record[0] is empty for sure, because we're splitting on RECORD_START. Just to play safe adding the check for all records.
        res.add(parseOneRecord(record));
      }
    }
    return res;
  }

  /**
   * Parses a single record returned by ' log'. The record contains information from pattern and file status and path (if respective
   * flags were provided).
   * @param line record to be parsed.
   * @return LogRecord with information about the revision.
   */
  LogRecord parseOneRecord(String line) {
    //using -fmt "%En|%Vn|%Nd|%o/%e|%u|%Nc\n|$~DELIM&^\n"
    /*
    base.iml|\main\bl144_integration\2|20101103.063829|checkin/create version|ascher
    |phnix00119574 [...].
    |$~DELIM&^
     */

    if (line.isEmpty()) { return null; }

    if(line.indexOf(RECORD_START) == 0) {
      line = line.substring(RECORD_START.length());
    }

    // parsing status and path (if given)
    char nameStatus = 0;
    final List<String> paths = new ArrayList<String>(1);
    final boolean includeStatus = myNameStatusOutputted == NameStatus.STATUS;
    final List<List<String>> parts = includeStatus ? new ArrayList<List<String>>() : null;

    if (myNameStatusOutputted != NameStatus.NONE) {
      final String[] infoAndPath = line.split(RECORD_END);
      line = infoAndPath[0];
      if (infoAndPath.length > 1) {
        // separator is \n for paths, space for paths and status
        final List<String> nameAndPathSplit = new ArrayList<String>(Arrays.asList(infoAndPath[infoAndPath.length - 1].split("\n")));
        for (Iterator<String> it = nameAndPathSplit.iterator(); it.hasNext();) {
          if (it.next().trim().isEmpty()) {
            it.remove();
          }
        }

        for (String pathLine : nameAndPathSplit) {
          String[] partsArr;
          if (includeStatus) {
            final int idx = pathLine.indexOf("\t");
            if (idx != -1) {
              final String whatLeft = pathLine.substring(idx).trim();
              partsArr = whatLeft.split("\\t");
              final List<String> strings = new ArrayList<String>(partsArr.length + 1);
              strings.add(pathLine.substring(0, 1));
              strings.addAll(Arrays.asList(partsArr));
              parts.add(strings);
            } else {
              partsArr = pathLine.split("\\t"); // should not
            }
          } else {
            partsArr = pathLine.split("\\t");
          }
          paths.addAll(Arrays.asList(partsArr));
        }
      }
    } else {
      line = line.substring(0, line.length()-1); // removing the last character which is RECORD_END
    }

    // parsing revision information
    // we rely on the order of options
    final String[] values = line.split(ITEMS_SEPARATOR);
    final Map<LogOption, String> res = new HashMap<LogOption, String>(values.length);
    int i = 0;
    for (; i < values.length && i < myOptions.length; i++) {  // fill valid values
      res.put(myOptions[i], values[i]);
    }
    for (; i < myOptions.length; i++) {  // options which were not returned are set to blank string, extra options are ignored.
      res.put(myOptions[i], "");
    }
    return new LogRecord(res, paths, parts);
  }
}
