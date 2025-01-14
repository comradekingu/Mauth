package com.xinto.mauth.otp.generator

import com.xinto.mauth.otp.OtpDigest
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.math.pow

interface OtpGenerator {

    fun generateHotp(
        secret: ByteArray,
        counter: Long,
        digits: Int = 6,
        digest: OtpDigest = OtpDigest.Sha1
    ): String

    fun generateTotp(
        secret: ByteArray,
        interval: Long,
        seconds: Long,
        digits: Int = 6,
        digest: OtpDigest = OtpDigest.Sha1
    ): String

}

class OtpGeneratorImpl : OtpGenerator {

    override fun generateHotp(
        secret: ByteArray,
        counter: Long,
        digits: Int,
        digest: OtpDigest
    ): String {
        val hash = Mac.getInstance(digest.algorithmName).let { mac ->
            val byteCounter = ByteBuffer.allocate(8)
                .putLong(counter)
                .array()

            mac.init(SecretKeySpec(secret, "RAW"))
            mac.doFinal(byteCounter)
        }

        val offset = hash[hash.size - 1].toInt() and 0xF

        val code = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            ((hash[offset + 3].toInt() and 0xff))

        val paddedCode = (code % 10.0.pow(digits.toDouble())).toInt()

        return StringBuilder(paddedCode.toString()).apply {
            while (length < digits) {
                insert(0, "0")
            }
        }.toString()
    }

    override fun generateTotp(
        secret: ByteArray,
        interval: Long,
        seconds: Long,
        digits: Int,
        digest: OtpDigest
    ): String {
        val counter = floor((seconds / interval).toDouble()).toLong()
        return generateHotp(secret, counter, digits, digest)
    }

}