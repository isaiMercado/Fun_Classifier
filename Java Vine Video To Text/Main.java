package Main;

import Types.VinePostsResponse;
import static Util.HttpUtil.getInputStreamHttpResponse;
import VineService.VineService;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.WordResult;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author isai
 */
public class VineFilters {

    
    public final static int SAMPLE_RATE = 16000;
    
    
    public static void main(String[] args) {
       
        String videoUrl = getVideoUrl();
        String videoPath = "~/VineAPI/mp3/videoTest.mp4";
        String audioPath = "~/VineAPI/mp3/audioTest.wav";
        String textPath = "~/VineAPI/mp3/textTest.txt";

        downloadVideo(videoUrl, videoPath);
        extractAudio(videoPath, audioPath);
        extractText(audioPath, textPath);
    }


    public static String getVideoUrl() {
        String videoUrl = "";
        
        try {

            VineService vineService = new VineService("userName", "password");
            vineService.authenticate();
            
            VinePostsResponse result1 = vineService.popular(1, 6);
            
            //VinePostsResponse result2 = vineService.promoted(1, 6);	

//            VinePostsResponse result3 = vineService.getTagPosts("cat", 1, 6);
//
//            VineTagsResponse result4 = vineService.searchTag("cat", 1, 6);
//
//            VinePostsResponse result5 = vineService.userLikes(1, 6);
//
//            VinePostsResponse result6 = vineService.userLikes("906802012891529216", 1, 6);
//
//            VinePostsResponse result7 = vineService.userTimeline(1, 6);

            
            //List<VineUser> result8 = vineService.searchUser("WillSasso");
            //String userID = String.valueOf(result8.get(0).userId);
            //VinePostsResponse result9 = vineService.userTimeline(userID, 1, 6);
            
            
            //VineNotificationsResponse result10 = vineService.notifications(1, 6);

            //VineUser result11 = vineService.me();

            //VineUser result12 = vineService.profile(906802012891529216); 
            
            videoUrl = result1.posts.get(3).videoLowURL;
            
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return videoUrl;
    }
    

    public static void downloadVideo(String videoUrl, String videoOutputPath) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(videoUrl);
            HttpResponse response = httpClient.execute(httpGet);
            InputStream stream = getInputStreamHttpResponse(response); 
            File targetFile = new File(videoOutputPath);
            FileUtils.copyInputStreamToFile(stream, targetFile);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }


    // RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 16000 Hz
    // RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz
    public static void extractAudio(String videoInputPath, String audioOutputPath) {
        try {
            File source = new File(videoInputPath);
            File target = new File(audioOutputPath);
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("pcm_s16le"); // pcm signed 16 bits little endian
            audio.setSamplingRate(new Integer(SAMPLE_RATE));
            audio.setChannels(new Integer(1));
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("wav");
            attrs.setAudioAttributes(audio);
            Encoder encoder = new Encoder();
            encoder.encode(source, target, attrs);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }


    public static void extractText(String audioInputPath, String textOutputPath) {

        try {

            Configuration configuration = new Configuration();
            configuration.setSampleRate(SAMPLE_RATE);
            configuration.setAcousticModelPath("/home/isai/CS465/VineAPI/speechToTextModels/en-us/en-us");
            configuration.setDictionaryPath("/home/isai/CS465/VineAPI/speechToTextModels/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("/home/isai/CS465/VineAPI/speechToTextModels/en-us/en-us.lm.bin");

            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            recognizer.startRecognition(new FileInputStream(audioInputPath));
            SpeechResult result = recognizer.getResult();
            recognizer.stopRecognition();

            String hypothesis = "";//result.getHypothesis();

            for (String string : result.getNbest(10)) {
                hypothesis = hypothesis + string + "\n";
            }

            hypothesis = hypothesis + "\nWords: " + result.getWords().toString();

            File targetFile = new File(textOutputPath);
            FileUtils.writeStringToFile(targetFile, hypothesis);

        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
        
      
}
