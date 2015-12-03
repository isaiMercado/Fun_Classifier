

#include "Poco/Net/HTTPClientSession.h"
#include "Poco/Net/HTTPRequest.h"
#include "Poco/Net/HTTPResponse.h"
#include "Poco/StreamCopier.h"
#include "Poco/Path.h"
#include "Poco/URI.h"
#include "Poco/Exception.h"
#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <fstream>
#include <iterator>
#include <algorithm>
#include <pocketsphinx/pocketsphinx.h>

extern "C" {
#include <libavutil/imgutils.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
}

#define MODELDIR "/usr/local/share/pocketsphinx/model"

using namespace Poco::Net;
using namespace Poco;
using namespace std;


template <typename T>
std::ostream& operator << (std::ostream& os, std::vector<T>& vec)
{
    std::copy(vec.begin(), vec.end(), std::ostream_iterator<T>(os));
    return os;
}


template <typename T>
std::vector<T>& operator << (std::vector<T>& vec, std::istream& is)
{
    vec.assign(std::istreambuf_iterator<T>(is), std::istreambuf_iterator<T>());
    return vec;
}

/**
    Things to do
    - save the stream in a buffer (maybe a vector<char> by using above functions)
*/
void getVideo(string videoUrl, string videoPath) {

    try
    {
        // prepare session
        URI uri(videoUrl);
        HTTPClientSession session(uri.getHost(), uri.getPort());

        // prepare path
        string path(uri.getPathAndQuery());
        if (path.empty()) path = "/";

        // send request
        HTTPRequest req(HTTPRequest::HTTP_GET, path, HTTPMessage::HTTP_1_1);
        session.sendRequest(req);

        // get response
        HTTPResponse response;
        istream& responseStream = session.receiveResponse(response);

        // save file
        ofstream myfile;
        myfile.open (videoPath);
        myfile << responseStream.rdbuf();
        myfile.close();
    }
    catch (Exception &ex)
    {
        cerr << ex.displayText() << endl;
    }
}

/**
    Things to do
    - Instead of running ffmpeg as an executable, someone that understand video and audio encoding can use the ffmpeg
    libraries functions to separate the audio programmatically from the buffer received in the getVideo function
*/
void getAudio(string audioPath, string videoPath) {
    string executable = "/opt/ffmpeg/bin/ffmpeg -loglevel quiet -y  -i " + videoPath + " -vn -ar 16000 -ac 1 -f wav -acodec pcm_s16le " + audioPath;
    system(executable.c_str());
}

/**
    Things to do
    - see if the model can be optimized to extract text more accuratelly
*/
void getText(string textPath,string audioPath) {
    ps_decoder_t *ps;
    cmd_ln_t *config;
    FILE *fh;
    char const *hyp, *uttid;
    int16 buf[512];
    int rv;
    int32 score;

    config = cmd_ln_init(NULL, ps_args(), TRUE,
                 "-hmm", MODELDIR "/en-us/en-us",
                 "-lm", MODELDIR "/en-us/en-us.lm.bin",
                 "-dict", MODELDIR "/en-us/cmudict-en-us.dict",
                 NULL);
    if (config == NULL) {
        fprintf(stderr, "Failed to create config object, see log for details\n");
        return;
    }

    ps = ps_init(config);
    if (ps == NULL) {
        fprintf(stderr, "Failed to create recognizer, see log for details\n");
        return;
    }

    fh = fopen(audioPath.c_str(), "rb");
    if (fh == NULL) {
        fprintf(stderr, "Unable to open input file goforward.raw\n");
        return;
    }

    rv = ps_start_utt(ps);

    while (!feof(fh)) {
        size_t nsamp;
        nsamp = fread(buf, 2, 512, fh);
        rv = ps_process_raw(ps, buf, nsamp, FALSE, FALSE);
    }

    rv = ps_end_utt(ps);
    string output = ps_get_hyp(ps, &score);

    fclose(fh);
    ps_free(ps);
    cmd_ln_free_r(config);

    ofstream myfile;
    myfile.open (textPath);
    myfile << output;
    myfile.close();
}

/**
    Things to do
    - we need to make a Vine API Client with Vine model Classes to wrap the JSON strings from the Vine Server
*/
int main(int argc, char** argv)
{
    string videoUrl = "http://v.cdn.vine.co/r/videos_r2/82D71250BC1278437629121708032_4f1b8e31a88.0.0.12646126430287638110.mp4?versionId=Hpy03tcE7FWokp46FGY3Irjfe9ZoYc11";
    string videoUrlLowQuality = "http://v.cdn.vine.co/r/videos/82D71250BC1278437629121708032_4f1b8e31a88.0.0.12646126430287638110.mp4?versionId=_Y3Eff5VDTtnEQc5Yud3s5fWRJ5M0cGr"; //works
    string videoPath = "video.mp4";
    string audioPath = "audio.wav";
    string textPath = "text.txt";

    getVideo(videoUrl, videoPath);
    getAudio(audioPath, videoPath);
    getText(textPath,audioPath);

    return 0;
}
