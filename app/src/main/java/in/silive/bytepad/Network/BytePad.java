package in.silive.bytepad.Network;

import in.silive.bytepad.Models.PaperModel;
import retrofit.http.GET;

/**
 * Created by akriti on 2/8/16.
 */
public interface BytePad {
    @GET("/getallpapers")
    PaperModel.PapersList papersList();
}