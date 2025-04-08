import logic.Booking.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BookingSpec extends AnyFlatSpec with Matchers:
   val formatter = DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" )

   "areDateRangeOverlapped" should "return true if the dates are in order: start1 - start2 - end2 - end1" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "8-04-2025 19:00", formatter )
      val end2 = LocalDateTime.parse( "8-04-2025 20:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe true
   }

   it should "return true if the dates are in order: start1 - start2 - end1 - end2" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "8-04-2025 19:00", formatter )
      val end2 = LocalDateTime.parse( "11-04-2025 20:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe true
   }

   it should "return true if the dates are in order: start2 - start1 - end2 - end1" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "6-04-2025 19:00", formatter )
      val end2 = LocalDateTime.parse( "9-04-2025 20:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe true
   }

   it should "return true if the dates are in order: start2 - start1 - end1 - end2" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "6-04-2025 19:00", formatter )
      val end2 = LocalDateTime.parse( "11-04-2025 20:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe true
   }

   it should "return true if the dates are identical" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end2 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe true
   }

   it should "return false if end1 = start2" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "10-04-2025 20:00", formatter )
      val end2 = LocalDateTime.parse( "10-04-2025 21:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe false
   }

   it should "return false if end2 = start1" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "10-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "7-04-2025 18:00", formatter )
      val end2 = LocalDateTime.parse( "7-04-2025 19:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe false
   }

   it should "return false if the dates are in order: start1 - end1 - start2 - end2" in {
      val start1 = LocalDateTime.parse( "7-04-2025 09:00", formatter )
      val end1 = LocalDateTime.parse( "7-04-2025 10:00", formatter )

      val start2 = LocalDateTime.parse( "7-04-2025 12:00", formatter )
      val end2 = LocalDateTime.parse( "7-04-2025 13:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe false
   }

   it should "return false if the dates are in order: start2 - end2 - start1 - end1" in {
      val start1 = LocalDateTime.parse( "7-04-2025 19:00", formatter )
      val end1 = LocalDateTime.parse( "8-04-2025 20:00", formatter )

      val start2 = LocalDateTime.parse( "2-04-2025 12:00", formatter )
      val end2 = LocalDateTime.parse( "4-04-2025 13:00", formatter )

      areDateRangeOverlapped( start1, end1, start2, end2 ) shouldBe false
   }