import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest extends TestCase {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated, stick, square, rotated ;
	private Piece[] pieces;

	protected void setUp() throws Exception {
		super.setUp();
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		stick = new Piece(Piece.STICK_STR);
		square = new Piece(Piece.SQUARE_STR);
		rotated= new Piece(Piece.L1_STR);

		sRotated = s.computeNextRotation();

		pieces = Piece.getPieces();
	}
	
	// Here are some sample tests to get you started
	
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());



		Piece s = new Piece(Piece.S1_STR);
		assertEquals(3, s.getWidth());
		assertEquals(2, s.getHeight());
		TPoint point = new TPoint(5,4);
		assertTrue(s.equals(s));
		assertFalse(s.equals(point));
		assertFalse(s.equals(stick));
		rotated = rotated.computeNextRotation();
		assertFalse(s.equals(rotated));
		assertFalse(s.equals(square));

		Piece five = new Piece("0 0 1 0 0 1 1 1  2 1");
		assertFalse(s.equals(five));

		Piece sCopy =  new Piece(Piece.S1_STR);
		assertTrue(s.equals(sCopy));
		s = s.computeNextRotation();
		assertFalse(s.equals(rotated));
		assertFalse(s.equals(square));
		assertEquals(2, s.getWidth());
		assertEquals(3, s.getHeight());

		TPoint[] points = s.getBody();
		TPoint[] comp = new TPoint[4];
		TPoint p = new TPoint(1,0);
		TPoint p1 = new TPoint(1,1);
		TPoint p2 = new TPoint(0,1);
		TPoint p3 = new TPoint(0,2);
		assertTrue(points[0].equals(p));
		assertTrue(points[1].equals(p1));
		assertTrue(points[2].equals(p2));
		assertTrue(points[3].equals(p3));

		Piece pce = pieces[0];
		pce = pce.fastRotation();

	}
	
	
	// Test the skirt returned by a few pieces
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));

	}

	@Test
	public void testExpectedRuleException(){
		String badSt		= "-0 s	0 0	 0 2  1 0";
		Piece bad;
		try {
			bad = new Piece(badSt);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("Could not parse x,y string"));
		}

	}

}
