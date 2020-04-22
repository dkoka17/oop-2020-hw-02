import junit.framework.TestCase;
import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static javax.swing.text.SimpleAttributeSet.EMPTY;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;


public class BoardTest extends TestCase {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	
	protected void setUp() throws Exception {
		b = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		b.place(pyr1, 0, 0);
	}
	
	// Check the basic width/height/max after the one placement
	public void testSample1() {
		assertEquals(6,b.getHeight());
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());

 		result = b.clearRows();
		assertEquals(1, result);
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(3, b.getColumnHeight(1));
		assertEquals(2, b.getColumnHeight(2));
		assertEquals(3, b.getMaxHeight());
		assertEquals(2, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(1, b.getRowWidth(2));
		assertEquals(0, b.getRowWidth(3));
		assertEquals(0, b.getRowWidth(4));
		assertEquals(3, b.getMaxHeight());


		result = b.dropHeight(sRotated,1);
		assertEquals(2, result);
		result = b.dropHeight(sRotated,-2);
		assertEquals(0, result);
		result = b.dropHeight(sRotated,3);
		assertEquals(0, result);
		result = b.dropHeight(sRotated,1);
		assertEquals(2, result);

		b.undo();
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());

		b.undo();
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());


		result = b.place(sRotated, 0, 1);
		assertEquals(Board.PLACE_BAD, result);


		result = b.place(sRotated, 2, 1);
		assertEquals(Board.PLACE_OUT_BOUNDS, result);
		result = b.place(sRotated, 1, 5);
		assertEquals(Board.PLACE_OUT_BOUNDS, result);
		result = b.place(sRotated, -1, 1);
		assertEquals(Board.PLACE_OUT_BOUNDS, result);
		result = b.place(sRotated, 1, -1);
		assertEquals(Board.PLACE_OUT_BOUNDS, result);

		assertTrue(b.getGrid(1,2));
		assertTrue(b.getGrid(-1,2));
		assertTrue(b.getGrid(1,-2));
		assertTrue(b.getGrid(4,2));
		assertTrue(b.getGrid(1,6));

		String  board = b.toString();
		System.out.println(board);
	}
	
	// Makre  more tests, by putting together longer series of 
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.


	public void testStrangeHeight(){
		Board exc = new Board(3, 10);
		exc.place(sRotated, 1, 6);
		exc.commit();
		//System.out.println(exc.toString());
		assertEquals(0, exc.getColumnHeight(0));
		assertEquals(9, exc.getColumnHeight(1));
		assertEquals(8, exc.getColumnHeight(2));
		assertEquals(9, exc.getMaxHeight());

		System.out.println(exc.toString());

		exc.place(sRotated, 0, 0);

		System.out.println(exc.toString());
	}

	@Test
	public void testExpectedRuleException(){
		Board exc = new Board(3, 6);
		exc.place(sRotated, 1, 1);
		try {
			exc.place(sRotated, 1, 1);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("place commit problem"));
		}

	}

	public void testreMaxHeight() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> secretClass = b.getClass();

		Field fields[] = secretClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.getName()=="maxHeight"){
				field.setInt(b,12);
			}
		}
		try {
			b.sanityCheck();
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("maxHeight incorrect"));
		}
	}
	public void testreHeight() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> secretClass = b.getClass();
		int[] arr = new int[3];
		arr[0]=12;
		arr[1]=2;
		arr[2]=4;
		Field fields[] = secretClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.getName()=="colHeight"){
				field.set(b,arr);
			}
		}
		try {
			b.sanityCheck();
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("Heights incorrect"));
		}
	}
	public void testWidth() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> secretClass = b.getClass();
		int[] arr = new int[6];
		arr[0]=12;
		arr[1]=2;
		arr[2]=4;
		arr[3]=12;
		arr[4]=2;
		arr[5]=4;
		Field fields[] = secretClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.getName()=="rowLength"){
				field.set(b,arr);
			}
		}
		try {
			b.sanityCheck();
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("widths incorrect"));
		}
	}

	public void testDEBUG() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> secretClass = b.getClass();
		Field fields[] = secretClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.getName()=="DEBUG"){
				field.setBoolean(b,false);
			}
		}
		b.sanityCheck();
	}






}
