package in.silive.bo;

import android.arch.persistence.room.PrimaryKey;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by root on 12/8/17.
 */

public class SubjectDatabaseModel {

    @PrimaryKey(autoGenerate = true)
    public Integer id;

    public String subjectCode;

    public String subjectName;

    /**
     * No args constructor for use in serialization
     */
    public SubjectDatabaseModel() {
    }


    public SubjectDatabaseModel(Integer id, String subjectCode, String subjectName) {
        super();
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }
}