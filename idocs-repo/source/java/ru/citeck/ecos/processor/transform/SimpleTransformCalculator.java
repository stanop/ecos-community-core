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

import java.awt.geom.AffineTransform;
import java.util.List;

public class SimpleTransformCalculator implements AffineTransformCalculator
{
	private List<SimpleTransform> transforms;

	@Override
	public AffineTransform calculate(double width, double height,
			double pageWidth, double pageHeight) 
	{
		AffineTransform result = new AffineTransform();
		double[] params = new double[] { 1, width, height, pageWidth, pageHeight, pageWidth/width, pageHeight/height };
		for(SimpleTransform transform : transforms) {
			transform.transform(result, params);
		}
		return result;
	}

	public void setTransforms(List<SimpleTransform> transforms) {
		this.transforms = transforms;
	}
	
}
