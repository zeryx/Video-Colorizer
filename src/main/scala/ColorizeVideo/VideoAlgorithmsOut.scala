package ColorizeVideo

/**
  * Created by james on 21/07/16.
  */
import com.google.gson.Gson
import scala.collection.JavaConverters._

case class VideoAlgorithmsOut(uuid: String, files: java.util.List[String]){

  def sortFiles: VideoAlgorithmsOut = {
    val newFiles = this.files.asScala.sortWith(_ < _).asJava
    new VideoAlgorithmsOut(this.uuid, newFiles)
  }
}

object VideoAlgorithmsOut{
  val gson = new Gson()

  def apply(input: String): VideoAlgorithmsOut = {
    gson.fromJson[VideoAlgorithmsOut](input, classOf[VideoAlgorithmsOut])
  }
}