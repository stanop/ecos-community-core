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
package ru.citeck.ecos.processor.transform;

public abstract class AbstractSimpleTransform implements SimpleTransform
{
	protected double[] x;
	protected double[] y;

	protected double multiply(double[] params, double[] scales) {
		double result = 0.0;
		for(int i = 0; i < params.length && i < scales.length; i++) {
			result += params[i] * scales[i];
		}
		return result;
	}

	private double[] parseValues(String s) {
		String[] ss = s.split("\\s+");
		double[] values = new double[ss.length];
		for(int i = 0; i < ss.length; i++) {
			values[i] = Double.parseDouble(ss[i]);
		}
		return values;
	}
	
	public void setXy(String xy) {
		double[] values = parseValues(xy);
		this.x = values;
		this.y = values;
	}
	
	public void setX(String x) {
		this.x = parseValues(x);
	}

	public void setY(String y) {
		this.y = parseValues(y);
	}

}
