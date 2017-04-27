package ru.citeck.ecos.utils;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class BigNumberEncoderTest {

	private static String[] numberStrings = new String[] {
		"0", "1", "10", "10000", "1234567890.1234567890", "1234567890.123456789"
	};
	
	private static abstract class Expander {
		public abstract String expand(String num);
	}
	
	private static Expander[] expanders = new Expander[] {
		new Expander() {
			public String expand(String num) { return "-" + num; }
		}, 
		new Expander() {
			public String expand(String num) { return num.indexOf('.') == -1 ? num + ".00" : num + "00"; }
		},
		new Expander() {
			public String expand(String num) { return num + "E-1000"; }
		},
		new Expander() {
			public String expand(String num) { return num + "E+1000"; }
		},
	};

	private static String[] expandNumberStrings(String[] numbers, Expander[] expanders) {
		String[] expanded = new String[numbers.length * (1 + expanders.length)];
		int i, j = 0;
		for(i = 0; i < numbers.length; i++) {
			expanded[j++] = numbers[i];
			for(int k = 0; k < expanders.length; k++) {
				expanded[j++] = expanders[k].expand(numbers[i]);
			}
		}
		return expanded;
	}
	
	@Test
	public void test() {
		
		String[] numbers = expandNumberStrings(numberStrings, expanders);
		
		for(int i = 0; i < numbers.length-1; i++) {
			for(int j = i+1; j < numbers.length; j++) {
				BigDecimal a = new BigDecimal(numbers[i]);
				BigDecimal b = new BigDecimal(numbers[j]);
				String sa = BigNumberEncoder.encode(a);
				String sb = BigNumberEncoder.encode(b);
				if(compare(a, b) != compare(sa, sb)) {
					System.err.println(a + " compared to " + b + " = " + compare(a, b));
					System.err.println(sa + " compared to " + sb + " = " + compare(sa, sb));
					fail("Comparison test failed");
				}
				
			}
		}
		
	}
	
	private static <T> int compare(Comparable<T> a, T b) {
		int x = a.compareTo(b);
		if(x < 0) return -1;
		if(x > 0) return 1;
		return 0;
	}
	
}
