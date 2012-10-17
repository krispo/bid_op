package domain.pojo

import scala.reflect._


@BeanInfo
case class Banner(
  val id: Long,
  val network_banner_id: String
) extends domain.Banner {}
