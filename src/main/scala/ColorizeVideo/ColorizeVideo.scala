package ColorizeVideo

/**
  * Created by james on 12/06/16.
  */
import java.util.concurrent.TimeUnit

import com.algorithmia.algo._
import com.algorithmia._
import com.google.gson.Gson
import org.apache.commons.io.FilenameUtils
import play.api.libs.json._

import scala.collection.JavaConverters._

/**
  * Created by james on 22/02/16.
  * This tool splits a black & white video file into small chunks, then splits them into frames, colorizes them, and then stitches them back together.
  */
class ColorizeVideo {

  val gson = new Gson()

  val vdTemp = "data://.my/temp"
  val outPath = "dropbox://algorithmia"
  val procTemp = "data://.my/temp"
  val VIDEO_PROCESS = "media/VideoAlgorithms/0.2.3"
  val COLOR_PROCESS = "algorithmiahq/ColorfulImageColorization/0.1.3"
  val ALGORITHMIA_API_KEY = ""
  val pattern = """-(\d{5})""".r
  def apply(data: Input, threads: Int): Exit = {
    try {
      val client = Algorithmia.client(ALGORITHMIA_API_KEY, threads)

      val extension = FilenameUtils.getExtension(data.src)

      Console.println(s"splitting to sub videos")

      val splitVideoDef = SplitToSubs(SplittingInputs(data.src), Outputs(vdTemp, "chunks", "mp4", false))
      val splitVideoFormatted = InputFormat("SplitToSubvideos", splitVideoDef)
      val subVideos: Exit = Exit(client.algo(s"algo://$VIDEO_PROCESS").pipe(splitVideoFormatted).asJsonString())


      Console.println("splitting to subVideos done, branching...")
      val f_SplitImages: List[FutureAlgoResponse] = subVideos.files.asScala.map((subVideo: String) =>{
        val splitImagesDef = SplitToImages(SplittingInputs(subVideo), Outputs(vdTemp, "frames", "png", false), data.fps)
        val splitImagesFormatted = InputFormat("SplitToFrames", splitImagesDef)
        client.algo(s"algo://$VIDEO_PROCESS").pipeAsync(splitImagesFormatted)
      }).toList

      val SplitImages: List[(String, String)] = f_SplitImages.flatMap((response: FutureAlgoResponse) => {
        val completedBatch = Exit(response.get().asJsonString())
        Console.println(s"completed batch #: ${completedBatch.uuid}")
        //create the SplitImages into a uuid & file tuple.
        completedBatch.files.asScala.map(file=> (file, completedBatch.uuid))
        })
      SplitImages.foreach(Console.println)

      //      colorize each frame of the movie

      Console.println("all image split batches complete, let's colorize them!")

      val f_ColorizeImages: List[(FutureAlgoResponse, String)]  = SplitImages.map((split: (String, String)) =>{
        val colorizeIt = ColorizeIt(split._1, s"$procTemp/${split._1.split('/').last}")
        (client.algo(s"algo://$COLOR_PROCESS").setTimeout(500l, TimeUnit.SECONDS).pipeAsync(colorizeIt), split._2)
      })


      //now that we have all of the colorizeImages running lets make them all real and wait for it to complete before encoding.

//      val colorizedImageUUIDs: List[String] = f_ColorizeImages.map{case (response, uuid) =>{
//        val colorized = Json.parse(response.get().asJsonString())
//        uuid
//      }}.distinct
val colorizedImageUUIDs: List[String] = f_ColorizeImages.map{case (response, uuid) =>{
  val colorized = Json.parse(response.get().asJsonString())
  uuid
}}.distinct
      //all images are colourized, now lets encode them back together.


      Console.println("all images colorized, encoding subVideos...")

      val f_encodeSubVideo = colorizedImageUUIDs.map((uuid: String) =>{
        val combineFromImages = CombineFromImages(CombineInputs(List(uuid).asJava, List(procTemp).asJava), Outputs(vdTemp, "colorized_chunk", "mp4", false), data.fps)
        val combineframesFormatted = InputFormat("CombineFromFrames", combineFromImages)
        client.algo(s"algo://$VIDEO_PROCESS").pipeAsync(combineframesFormatted)
      })

      val encodedSubVideos: List[String] = f_encodeSubVideo.map(s => Exit(s.get().asJsonString()).uuid)
      Console.println("all subvideos encoded concating back to full video.")

      val combineFromSubs = CombineFromSubs(CombineInputs(encodedSubVideos.asJava, List(vdTemp).asJava), Outputs(outPath, "colorized_video", "mp4", false), data.fps)
      val combineSubsFormatted = InputFormat("CombineFromSubvideos", combineFromSubs)
      val recombinedVideo: Exit = Exit(client.algo(s"algo://$VIDEO_PROCESS").pipe(combineSubsFormatted).asJsonString())
      recombinedVideo

      //now that the frames are colorized, lets pass them all back and rejoin them.


    } catch {
      case e: Exception => throw new AlgorithmException(s"exception thrown, demo Halted. \n  ${e.getMessage}", e)
    }
  }
}
