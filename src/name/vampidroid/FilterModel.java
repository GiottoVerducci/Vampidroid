package name.vampidroid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView.Tokenizer;

public class FilterModel {

	private ArrayList<String> mClans;

	private ArrayList<String> mTypes;

	private ArrayList<String> mDisciplinesLibrary;

	private ArrayList<String> mDisciplinesCrypt;

	private ArrayList<FilterModel.FilterToken> mFiltersCrypt = new ArrayList<FilterModel.FilterToken>();

	private ArrayList<FilterModel.FilterToken> mFiltersLibrary = new ArrayList<FilterModel.FilterToken>();
	
	private String mNameFilter = "";
	

	private enum TokenTypeEnum {
		CLAN("Clan = '?'"), TYPE("Type = '?'"), DISCIPLINE_LIBRARY(
				"Discipline like '%?%'"), DISCIPLINE_CRYPT(
				"Disciplines like '%?%'");

		private String mFilterQuery;

		private TokenTypeEnum(String filterQuery) {

			mFilterQuery = filterQuery;
		}

		public String getFilterQuery() {
			return mFilterQuery;
		}

	}

	private class FilterToken {

		TokenTypeEnum mTokenType;
		
		private final String CLAN_FILTER = "Clan = '?'";
		private final String TYPE_FILTER = "Type = '?'";
		private final String DISCIPLINE_CRYPT_FILTER = "Disciplines like '%?%'";
		private final String DISCIPLINE_CRYPT_PLUS_FILTER = "lower(Disciplines) like '%?%'";
		private final String DISCIPLINE_LIBRARY_FILTER = "Discipline like '%?%'";
		
		

		String mFilterQuery;

		List<FilterToken> mAndTokens;

		public FilterToken(String token) {

			// Create "and" tokens...
			// for&pro
			if (token.contains("&")) {

				mAndTokens = new ArrayList<FilterModel.FilterToken>();

				StringTokenizer st = new StringTokenizer(token, "&");

				while (st.hasMoreElements()) {
					String token2 = st.nextToken();
					if (token2.length() != 0)
						mAndTokens.add(new FilterToken(token2));

				}

			}
			
			
			else if (token.contains("+")) {
				
				// Create tokens which have the + which indicates discipline inferior and superior
				// for+ == (for or FOR)
				mFilterQuery = DISCIPLINE_CRYPT_PLUS_FILTER.replace("?", token.replace("+", ""));
				
				
			}
			

			else if (mClans.contains(token)) {

				mTokenType = TokenTypeEnum.CLAN;
				mFilterQuery = CLAN_FILTER.replace("?", token);

			}

			else if (mTypes.contains(token)) {

				mTokenType = TokenTypeEnum.TYPE;
				mFilterQuery = TYPE_FILTER.replace("?", token);

			}

			else if (mDisciplinesLibrary.contains(token)) {

				mTokenType = TokenTypeEnum.DISCIPLINE_LIBRARY;
				mFilterQuery = DISCIPLINE_LIBRARY_FILTER.replace("?", token);

			}

			else if (mDisciplinesCrypt.contains(token)) {

				mTokenType = TokenTypeEnum.DISCIPLINE_CRYPT;
				mFilterQuery = DISCIPLINE_CRYPT_FILTER.replace("?", token);

			}

//			if (mTokenType != null)
//				mFilterQuery = mTokenType.getFilterQuery().replace("?", token);

		}


		public String getFilterQuery() {

			if (mAndTokens != null) {
				StringBuilder sb = new StringBuilder();

				sb.append("(");

				for (Iterator iterator = mAndTokens.iterator(); iterator
						.hasNext();) {
					FilterToken token = (FilterToken) iterator.next();

					sb.append(token.getFilterQuery()).append(" and ");

				}
				sb.append(" 1=1 )"); // To not need to remove the last 'and'

				return sb.toString();
			}

			else

				return mFilterQuery;
		}
	}

	public FilterModel(List<String> clans, List<String> types,
			List<String> disciplinesLibrary, List<String> disciplinesCrypt) {

		mClans = new ArrayList<String>(clans);

		mTypes = new ArrayList<String>(types);

		mDisciplinesLibrary = new ArrayList<String>(disciplinesLibrary);

		mDisciplinesCrypt = new ArrayList<String>(disciplinesCrypt);

	}

	public List<String> getAllFilterStrings() {

		ArrayList<String> result = new ArrayList<String>();

		result.addAll(mClans);
		result.addAll(mTypes);
		result.addAll(mDisciplinesLibrary);
		result.addAll(mDisciplinesCrypt);

		return result;

	}

	public List<String> getCryptFilterStrings() {

		ArrayList<String> result = new ArrayList<String>();

		result.addAll(mClans);
		result.addAll(mDisciplinesCrypt);

		return result;

	}

	public List<String> getLibraryFilterStrings() {

		ArrayList<String> result = new ArrayList<String>();

		result.addAll(mClans);
		result.addAll(mTypes);
		result.addAll(mDisciplinesLibrary);

		return result;

	}

	
	public void buildCryptFiltersFromString(String s) {

		StringTokenizer st = new StringTokenizer(s, ",");

		while (st.hasMoreTokens()) {

			String token = st.nextToken().trim();

			if (token.length() == 0)
				continue;

			FilterToken filter = new FilterToken(token);

			mFiltersCrypt.add(filter);

		}

	}

	public void buildLibraryFiltersFromString(String s) {

		StringTokenizer st = new StringTokenizer(s, ",");

		while (st.hasMoreTokens()) {

			String token = st.nextToken().trim();

			if (token.length() == 0)
				continue;

			FilterToken filter = new FilterToken(token);

			mFiltersLibrary.add(filter);

		}

	}
	
	public void setNameFilter(String filter) {
		// TODO Auto-generated method stub
		
		mNameFilter = formatQueryString(filter.toLowerCase().trim());
		
	}
	
	public String getNameFilterQuery() {
		// TODO Auto-generated method stub
		
		if (mNameFilter.length() > 0)
			return " and lower(Name) like '%" + mNameFilter + "%'";
		else
			return "";
		
	}

//	public String getCryptFilterQuery() {
//
//		if (mFiltersCrypt.size() == 0)
//			return "";
//
//		StringBuilder result = new StringBuilder();
//
//		result.append(" and ( ");
//
//		for (Iterator<FilterToken> iterator = mFiltersCrypt.iterator(); iterator
//				.hasNext();) {
//
//			result.append(iterator.next().getFilterQuery()).append(" or ");
//
//		}
//
//		result.append(" 1=2 )"); // Needed to avoid removing of last or added.
//
//		
//		return result.toString();
//	}
//
//	public String getLibraryFilterQuery() {
//
//		if (mFiltersLibrary.size() == 0)
//			return "";
//
//		StringBuilder result = new StringBuilder();
//
//		result.append(" and ( ");
//
//		for (Iterator<FilterToken> iterator = mFiltersLibrary.iterator(); iterator
//				.hasNext();) {
//
//			result.append(iterator.next().getFilterQuery()).append(" or ");
//
//		}
//
//		result.append(" 1=2 )"); // Needed to avoid removing of last or added.
//
//		return result.toString();
//	}
	
	public String getCryptFilterQuery() {

		if (mFiltersCrypt.size() == 0)
			return "";

		StringBuilder result = new StringBuilder();

		result.append(" and ( ");

		for (Iterator<FilterToken> iterator = mFiltersCrypt.iterator(); iterator
				.hasNext();) {

			result.append(iterator.next().getFilterQuery()).append(" and ");

		}

		result.append(" 1=1 )"); // Needed to avoid removing of last and added.

		
		return result.toString();
	}

	public String getLibraryFilterQuery() {

		if (mFiltersLibrary.size() == 0)
			return "";

		StringBuilder result = new StringBuilder();

		result.append(" and ( ");

		for (Iterator<FilterToken> iterator = mFiltersLibrary.iterator(); iterator
				.hasNext();) {

			result.append(iterator.next().getFilterQuery()).append(" and ");

		}

		result.append(" 1=1 )"); // Needed to avoid removing of last and added.

		return result.toString();
	}

	

	public void clearAllFilters() {

		mFiltersCrypt.clear();
		mFiltersLibrary.clear();
	}

	public void clearCryptFilters() {

		mFiltersCrypt.clear();
	}

	public void clearLibraryFilters() {

		mFiltersLibrary.clear();
	}

	protected String formatQueryString(String stringExtra) {
	
		return stringExtra.trim().replace("'", "''");
	}

	public static class CommaEAmpTokenizer implements Tokenizer {
		public int findTokenStart(CharSequence text, int cursor) {
			int i = cursor;

			while (i > 0 && (text.charAt(i - 1) != ',')
					&& (text.charAt(i - 1) != '&')) {
				i--;
			}
			while (i < cursor && text.charAt(i) == ' ') {
				i++;
			}

			return i;
		}

		public int findTokenEnd(CharSequence text, int cursor) {
			int i = cursor;
			int len = text.length();

			while (i < len) {
				if ((text.charAt(i) == ',') || (text.charAt(i) == '&')) {
					return i;
				} else {
					i++;
				}
			}

			return len;
		}

		public CharSequence terminateToken(CharSequence text) {
			int i = text.length();

			while (i > 0 && text.charAt(i - 1) == ' ') {
				i--;
			}

			if (i > 0
					&& (text.charAt(i - 1) == ',' || text.charAt(i - 1) == '&')) {
				return text;
			} else {
				if (text instanceof Spanned) {
					SpannableString sp = new SpannableString(text + ", ");
					TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
							Object.class, sp, 0);
					return sp;
				} else {
					return text + ", ";
				}
			}
		}
	}

	

}
