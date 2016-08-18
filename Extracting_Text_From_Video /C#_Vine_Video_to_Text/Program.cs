using System;
using VineSharp;
using VineSharp.Responses;
using VineSharp.Requests;
using VineSharp.Models;
using Newtonsoft.Json;
using System.Linq;
using System.Collections.Generic;
using YoutubeExtractor;
using System.IO;
using System.Net;
using System.Diagnostics;

namespace VineAPI
{
	class MainClass
	{
		static void Main(string[] args)
		{
			string videoUrl = getVideoUrl();
			string videoPath = "~/VineAPI/download/test.mp4";
			string audioPath = "~/VineAPI/download/test.avi";
			string textPath = "/VineAPI/download/test.txt";

			downloadVideo(videoUrl, videoPath);
			extractAudio(videoPath, audioPath);
			extractText(audioPath, textPath);
		}


		private string getVideoUrl() {
			string videoUrl = "";
			try {
				var vineClient = new VineClient();
				vineClient.SetCredentials(Username, Password);

				//				var options = new VinePagingOptions
				//				{
				//				    Size = 5
				//				};
				//
				//				var result3 = vineClient.UserTimeline(928471164316164096).Result; // search timelines by user ID
				//				Console.Write(JsonConvert.SerializeObject(result3, Formatting.Indented));
				//
				//				var result4 = vineClient.TagTimeline("test", options).Result; // search timelines by tag text
				//				Console.Write(JsonConvert.SerializeObject(result4, Formatting.Indented));
				//
				//				var result5 = vineClient.Post(1118756259315761152).Result; // search post by post ID
				//				Console.Write(JsonConvert.SerializeObject(result5, Formatting.Indented));
				//
				var result7 = vineClient.PopularTimeline().Result;
				// getting single post
				VinePost timeline_post = result7.Data.Records.ElementAt(0);

				// getting identifiers
				long timeline_postID = timeline_post.PostId;
				long timeline_userID = timeline_post.UserId;
				// getting filtering information
				PagedWrapper<VineComment> timeline_comments = timeline_post.Comments;
				string timeline_videoUrl = timeline_post.VideoLowUrl; // low definition video to get audio
				string timeline_description = timeline_post.Description;
				bool timeline_hasBadContent = timeline_post.ExplicitContent; // check this one
				int timeline_likes = timeline_post.Likes.Count;
				decimal timeline_views = timeline_post.Loops.Count;
				List<VineEntity> tags = timeline_post.Entities;

				Console.Write(JsonConvert.SerializeObject(result7, Formatting.Indented));

				//var result = vineClient.MyProfile().Result;
				//Console.Write(JsonConvert.SerializeObject(result, Formatting.Indented));

				//var result2 = vineClient.UserProfile(1118680983320059904).Result; // search user profile by user ID
				//Console.Write(JsonConvert.SerializeObject(result2, Formatting.Indented));

				//var result6 = vineClient.Likes(1118756259315761152).Result;
				//Console.Write(JsonConvert.SerializeObject(result6, Formatting.Indented));

				//var result8 = vineClient.Comments(1102011480238436352).Result;
				//Console.Write(JsonConvert.SerializeObject(result8, Formatting.Indented));

				//var result9 = vineClient.MyFollowers().Result;
				//Console.Write(JsonConvert.SerializeObject(result9, Formatting.Indented));

				//var result10 = vineClient.UserFollowers(1100682554694316032).Result;
				//Console.Write(JsonConvert.SerializeObject(result10, Formatting.Indented));

				//var result11 = vineClient.UserFollowing(1100682554694316032).Result;
				//Console.Write(JsonConvert.SerializeObject(result11, Formatting.Indented));

				//var result12 = vineClient.MyFollowing().Result;
				//Console.Write(JsonConvert.SerializeObject(result12, Formatting.Indented));

				//var result13 = vineClient.AddLike(1102011480238436352).Result;
				//Console.Write(JsonConvert.SerializeObject(result13, Formatting.Indented));

				//var result14 = vineClient.RemovedLike(1102011480238436352).Result;
				//Console.Write(JsonConvert.SerializeObject(result14, Formatting.Indented));

				videoUrl = timeline_post.VideoLowUrl;
			}
			catch(AggregateException exception)
			{
				foreach (Exception ex in exception.InnerExceptions) {
					Console.WriteLine (ex.Message);
				}

			}
			catch(Exception ex) {
				Console.WriteLine ("\n Exception Message:  " + ex.Message);
				Console.WriteLine ("\n Exception Source:  " + ex.Source);
				Console.WriteLine ("\n Exception Stack:  " + ex.StackTrace);
			}
			return videoUrl;
		}


		private void downloadVideo(string videoUrl, string videoPath) {
			var webClient = new WebClient();
			byte[] data = webClient.DownloadData(videoUrl);

			var videoStream = new MemoryStream(data);

			var fileStream = File.Create(videoPath);
			videoStream.Seek(0, SeekOrigin.Begin);
			videoStream.CopyTo(fileStream);

			fileStream.Close();
			videoStream.Close();
			webClient.Dispose ();
		}


		public void extractAudio(string videoPath, string audioPath) {
			string parameters = "-loglevel quiet -y  -i " + videoPath + " -vn -ar 16000 -ac 1 -f wav -acodec pcm_s16le " + audioPath;
			try
			{
				ProcessStartInfo startInfo = new ProcessStartInfo ("~/VineAPI/ffmpeg.exe", parameters);
				startInfo.UseShellExecute = false;
				startInfo.CreateNoWindow = true;
				startInfo.RedirectStandardOutput = true;
				startInfo.RedirectStandardError = true;

				Process proc = System.Diagnostics.Process.Start(startInfo);
				proc.WaitForExit();
				proc.Close();
			}
			catch (Exception ex) {
				Console.WriteLine ("\n Exception Message:  " + ex.Message);
				Console.WriteLine ("\n Exception Source:  " + ex.Source);
				Console.WriteLine ("\n Exception Stack:  " + ex.StackTrace);
			}
		}

		private void extractText(string audioPath, string textPath) {
			// Looking for audio to text engine
		}

	}

}
