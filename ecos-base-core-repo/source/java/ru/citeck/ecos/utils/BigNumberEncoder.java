/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.utils;

import java.math.BigDecimal;

//NumericEncoder moved to org.alfresco.util package in alfresco 5
import org.alfresco.util.NumericEncoder;
import org.apache.commons.lang.StringUtils;

/**
 * Index-specific encoder of big decimal numbers.
 * For each number returns string, such as: if a < b, then encode(a) < encode(b)
 * 
 * @author Sergey Tiunov
 *
 */
public class BigNumberEncoder {
	
	/**
	 * Prepare big decimal for index storage.
	 * @param num - big decimal number string
	 * @return string, such as if a < b, then encode(a) < encode(b)
	 */
	public static String encode(BigDecimal num) {

		int sign = num.signum();
		int exponent = num.precision() - num.scale(); // order of first non-zero digit 
		
		String sigStr = sign < 0 ? "n" : "p"; // p > n
		String expStr = NumericEncoder.encode(exponent);
		// remove all trailing zeros and decimal point
		String numStr = num.toPlainString().replaceAll("\\.", "").replaceAll("0*$", ""); 

		if(sign < 0) {
			// we need to negate strings for negative numbers
			expStr = negateHex(expStr);
			numStr = negateDec(numStr);
		} else if(sign == 0) {
			// in 0 there is no non-zero digits
			// so we return always the same:
			sigStr = "o"; // n < o < p
			expStr = "";
			numStr = "0";
		}
		
		return sigStr + expStr + "-" + numStr;
	}
	
	private static String negateDec(String x) {
		return StringUtils.replaceChars(x, "0123456789", "9876543210");
	}

	private static String negateHex(String x) {
		return StringUtils.replaceChars(
				StringUtils.replaceChars(x, "ABCDEF", "abcdef"), 
				"0123456789abcdef", "fedcba9876543210");
	}

	public static String getMinValue() {
		return "m";
	}
	
	public static String getMaxValue() {
		return "r";
	}
	
}
