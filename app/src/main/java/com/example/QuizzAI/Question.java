package com.example.QuizzAI;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Question implements Parcelable {
    public String question;
    public List<String> options;
    public int answerIndex;
    public int userAnswer = -1;

    public Question() {}

    protected Question(Parcel in) {
        question = in.readString();
        options = new ArrayList<>();
        in.readStringList(options);
        answerIndex = in.readInt();
        userAnswer = in.readInt();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeStringList(options);
        dest.writeInt(answerIndex);
        dest.writeInt(userAnswer);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}