package main.scala

import java.security.MessageDigest

object Utils {
	def md5(s: String) = {
		MessageDigest.getInstance("MD5").digest(s.getBytes)
	}
}