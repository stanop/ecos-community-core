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

public class DiagonalCalculator implements AffineTransformCalculator
{

	@Override
	public AffineTransform calculate(double width, double height,
			double pageWidth, double pageHeight) 
	{
        double angle = Math.atan(pageHeight/pageWidth);
        double stampDiagonal = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));

        // very, very, very strange transform...
        AffineTransform transform = new AffineTransform();
        double factor = Math.min(pageWidth, pageHeight) / stampDiagonal;
        // first move it into center minus factored half
        transform.translate(pageWidth/2 - factor*width/2, pageHeight/2 - factor*height/2);
        // factor
        transform.scale(factor, factor);
        // rotate by center of stamp
        transform.rotate(angle, width/2, height/2);

        return transform;
	}

}
