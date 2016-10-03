package ColorizeVideo

import com.algorithmia.{AlgorithmException, AlgorithmiaClient}
import com.google.gson.Gson

import scala.util.matching.Regex

/**
  * Created by james on 13/06/16.
  */
object utils {
  val gson = new Gson()

  def getSubVideoID(nameOpt: Option[String], pattern: Regex): Int = {
    nameOpt match {
      case Some(name) => {

        (for (pattern(group) <- pattern.findFirstIn(name)) yield group) match {
          case Some(uuid) => {
            uuid.toInt
          }
          case _ => throw new AlgorithmException(s"filename does not contain a UUID: $name")
        }
      }
      case _ => {
        throw new AlgorithmException(s" no files returned from VideoAlgorithms.")
      }
    }
  }

  def getFrameID(name: String, pattern: Regex): Int = {
    val result = (for (pattern(group) <- pattern.findAllMatchIn(name)) yield group).toList
    result(1).toInt
  }

  def parseTime(millis: Long): String = {
    val milliseconds = millis % 1000
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60)) % 24

    val m = "%03d".format(milliseconds)
    val s = "%02d".format(seconds)
    val min = "%02d".format(minutes)
    val hr = "%02d".format(hours)

    s"$hr:$min:$s.$m"
  }
}