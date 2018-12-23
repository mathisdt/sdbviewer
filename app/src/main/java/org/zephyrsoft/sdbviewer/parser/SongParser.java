package org.zephyrsoft.sdbviewer.parser;

import org.zephyrsoft.sdbviewer.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a {@link Song} and thus prepares it for being shown in the editor or as presentation.
 */
public class SongParser {
	
	protected static final String LABEL_MUSIC = "Music: ";
	protected static final String LABEL_TEXT = "Text: ";
	protected static final String LABEL_TRANSLATION = "Translation: ";
	protected static final String LABEL_PUBLISHER = "Publisher: ";
	
	private static final Pattern TRANSLATION_PATTERN = Pattern.compile("^(.*)\\[(.*)\\](.*)$");
	private static final String NEWLINE_REGEX = "\r?+\n";
	
	private SongParser() {
		// this class should only be used statically
	}
	
	/**
	 * Breaks down a {@link Song} into its elements. See {@link SongElementEnum} for more information about the line
	 * break policy used in the returned list!
	 * 
	 * @param song
	 *            the song to parse
	 * @param includeTitle
	 *            should the title be included?
	 * @param includeChords
	 *            should all the chord lines be included?
	 * @return a list containing the elements, marked up using {@link SongElementEnum}s
	 */
	public static List<SongElement> parse(Song song, boolean includeTitle, boolean includeChords) {
		List<SongElement> ret = new ArrayList<>();
		
		// title
		if (includeTitle) {
			ret.add(new SongElement(SongElementEnum.TITLE, song.getTitle() == null ? "" : song.getTitle()));
		}
		
		// lyrics
		if (song.getLyrics() != null) {
			boolean isFirst = true;
			for (String line : song.getLyrics().split(NEWLINE_REGEX)) {
				Matcher translationMatcher = TRANSLATION_PATTERN.matcher(line);
				if (translationMatcher.matches()) {
					isFirst = addNewlineIfNotFirstLine(ret, isFirst);
					String prefix = translationMatcher.group(1);
					String translation = translationMatcher.group(2);
					String suffix = translationMatcher.group(3);
					if (!isEmpty(prefix)) {
						ret.add(new SongElement(SongElementEnum.LYRICS, prefix));
					}
					if (!isEmpty(translation)) {
						ret.add(new SongElement(SongElementEnum.TRANSLATION, translation));
					}
					if (!isEmpty(suffix)) {
						ret.add(new SongElement(SongElementEnum.LYRICS, suffix));
					}
				} else if (isChordsLine(line)) {
					if (includeChords) {
						isFirst = addNewlineIfNotFirstLine(ret, isFirst);
						ret.add(new SongElement(SongElementEnum.CHORDS, line));
					}
				} else {
					isFirst = addNewlineIfNotFirstLine(ret, isFirst);
					ret.add(new SongElement(SongElementEnum.LYRICS, line));
				}
				
			}
		}
		
		// copyright
		if (!isEmpty(song.getComposer())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_MUSIC + song.getComposer()));
		}
		if (!isEmpty(song.getAuthorText())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_TEXT + song.getAuthorText()));
		}
		if (!isEmpty(song.getAuthorTranslation())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_TRANSLATION + song.getAuthorTranslation()));
		}
		if (!isEmpty(song.getPublisher())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_PUBLISHER + song.getPublisher()));
		}
		if (!isEmpty(song.getAdditionalCopyrightNotes())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, song.getAdditionalCopyrightNotes()));
		}
		
		return ret;
	}
	
	private static boolean addNewlineIfNotFirstLine(List<SongElement> elementList, boolean isFirst) {
		boolean ret = isFirst;
		if (ret) {
			ret = false;
		} else {
			elementList.add(new SongElement(SongElementEnum.NEW_LINE, "\n"));
		}
		return ret;
	}
	
	/**
	 * Extract the first lyrics-only line from a song.
	 */
	public static String getFirstLyricsLine(Song song) {
		if (song.getLyrics() != null) {
			for (String line : song.getLyrics().split(NEWLINE_REGEX)) {
				Matcher translationMatcher = TRANSLATION_PATTERN.matcher(line);
				if (!translationMatcher.matches() && !isChordsLine(line)) {
					return line;
				}
			}
		}
		return "";
	}
	
	/**
	 * Determines if the given line contains only guitar chords.
	 */
	private static boolean isChordsLine(String line) {
		return percentOfSpaces(line) >= 0.5;
	}
	
	/**
	 * Calculates the percentage of spaces in the given string.
	 * 
	 * @return a value between 0.0 and 1.0
	 */
	private static double percentOfSpaces(String toParse) {
		int spacesCount = 0;
		for (int i = 0; i < toParse.length(); i++) {
			if (toParse.substring(i, i + 1).equals(" ")) {
				spacesCount++;
			}
		}
		if (toParse.length() != 0) {
			return (double) spacesCount / (double) toParse.length();
		} else {
			return 0.0;
		}
	}

	/**
	 * Indicates specific elements of a {@link Song}.
	 */
	private enum SongElementEnum {
		/** the title (if present, it is always exactly one line) */
		TITLE,
		/** a lyrics element (not always a whole line, see NEW_LINE) */
		LYRICS,
		/** a chord element (not always a whole line, see NEW_LINE) */
		CHORDS,
		/** a translation element (not always a whole line, see NEW_LINE) */
		TRANSLATION,
		/** a copyright element (always a whole line) */
		COPYRIGHT,
		/** indicates a line break between LYRICS, CHORDS and TRANSLATION elements - this element is only used there! */
		NEW_LINE;
	}

	/**
	 * Holds one element of a {@link Song}, e.g. one lyrics line or the title.
	 *
	 * @author Mathis Dirksen-Thedens
	 */
	private static class SongElement {

		private SongElementEnum type;
		private String element;

		public SongElement(SongElementEnum type, String element) {
			this.type = type;
			this.element = element;
		}

		public SongElementEnum getType() {
			return type;
		}

		public String getElement() {
			return element;
		}

		@Override
		public String toString() {
			return type + (element != null ? "[" + element + "]" : "");
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof SongElement && type == ((SongElement) obj).getType()
				&& equals(element, ((SongElement) obj).getElement());
		}

		private static boolean equals(String one, String two) {
			return (one == null && two == null) || (one != null && one.equals(two));
		}

	}

	private static boolean isEmpty(String toTest) {
		return toTest == null || toTest.isEmpty();
	}

}
