package name.vampidroid.data;

import android.arch.persistence.room.PrimaryKey;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

/**
 * Created by francisco on 11/09/17.
 */

public abstract class Card {

    @PrimaryKey(autoGenerate = false)
    private Long uid;
    private String name;
    private String text;
    private final String type;
    private final String clan;
    private final String disciplines;
    private final String setRarity;
    private final String artist;

    public Card(String name, String text, String type, String clan, String disciplines, String setRarity, String artist) {
        this.name = name;
        this.text = text;
        this.type = type;
        this.clan = clan;
        this.disciplines = disciplines;
        this.setRarity = setRarity;
        this.artist = artist;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public SpannableStringBuilder getTextWithStyle()
    {
        String text = getText();

        SpannableStringBuilder str = new SpannableStringBuilder(text);

        int startBold = -1;
        for(int i = 0; i < str.length(); ++i) {
            if(str.charAt(i) == '*'){
                if(startBold >= 0) {
                    str.delete(i, i+1);
                    str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), startBold, i-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    startBold = -1;
                }
                else {
                    startBold = i;
                    str.delete(i, i+1);
                }
                --i;
            }
        }

        return str;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public String getClan() {
        return clan;
    }

    public String getDisciplines() {
        return disciplines;
    }

    public String getSetRarity() {
        return setRarity;
    }

    public String getArtist() {
        return artist;
    }
}
