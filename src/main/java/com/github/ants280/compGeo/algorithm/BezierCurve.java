package com.github.ants280.compGeo.algorithm;

import com.github.ants280.compGeo.Binomial;
import com.github.ants280.compGeo.CompGeoUtils;
import com.github.ants280.compGeo.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BezierCurve
{
	private final List<Point> controlPoints;
	private final transient Binomial binomial;
	private final transient int n;

	public BezierCurve(Point... controlPoints)
	{
		this(Arrays.asList(controlPoints));
	}

	public BezierCurve(List<Point> controlPoints)
	{
		this.controlPoints = new ArrayList<>(controlPoints);
		this.n = this.controlPoints.size() - 1;
		this.binomial = new Binomial();

		if (n + 1 < 2)
		{
			throw CompGeoUtils.createIllegalArgumentException("Must have at least two control points", controlPoints);
		}
	}

	public List<Point> getPoints(final int stepCount)
	{
		return getPoints(0, 1, stepCount);
	}

	public List<Point> getPoints(final double tMin, final double tMax, final int stepCount)
	{
		return getPoints(tMin, tMax, stepCount, null);
	}

	/**
	 * Get the points on the Bezier curve between tMin and tMax using the
	 * specified stepCount with a short-circuiting maxPointDifference to return
	 * null.
	 *
	 * @param tMin The starting parametric value. Use 0 for complete Bezier
	 * curves.
	 * @param tMax The ending parametric value. Use 1 for complete Bezier
	 * curves.
	 * @param stepCount The number of steps to take between tMin and tMax.
	 * @param maxPointDifference The max linear distance between consecutive
	 * points.
	 * @return The points of the Bezier curve between tMin and tMax, or null if
	 * any of the points are farther than <code>maxPointDifference</code> apart.
	 */
	public List<Point> getPoints(final double tMin, final double tMax, final int stepCount, final Double maxPointDifference)
	{
		validateParametricValues(tMin, tMax, stepCount);

		if (stepCount == 0)
		{
			return Collections.singletonList(getPoint(tMin));
		}

		Point[] points = new Point[stepCount + 1];

		double stepAmount = (tMax - tMin) / stepCount;

		// B(t) = SUM(i=0,n, binomial(n,i) * t^i * (1-t)^(n-i) * P(i)
		for (int step = 0; step <= stepCount; step++)
		{
			double t = tMin + (step * stepAmount);
			points[step] = getPoint(t);

			if (maxPointDifference != null
					&& step > 0
					&& CompGeoUtils.getDistance(points[step - 1], points[step]) > maxPointDifference)
			{
				return null;
			}
		}

		return Arrays.asList(points);
	}

	private Point getPoint(double t)
	{
		double x = 0;
		double y = 0;

		for (int i = 0; i <= n; i++)
		{
			double scale = binomial.of(n, i) * Math.pow(t, i) * Math.pow(1 - t, (double) n - i);
			if (scale < 0 || controlPoints.get(i).getX() < 0 || controlPoints.get(i).getY() < 0)
			{
				throw CompGeoUtils.createIllegalArgumentException(
						"Invalid scale or point [x,y]",
						scale,
						controlPoints.get(i).getX(),
						controlPoints.get(i).getY());
			}

			x += (scale * controlPoints.get(i).getX());
			y += (scale * controlPoints.get(i).getY());
		}

		return new Point(x, y);
	}

	private static void validateParametricValues(double tMin, double tMax, int stepCount) throws IllegalArgumentException
	{
		if (tMin < 0)
		{
			throw CompGeoUtils.createIllegalArgumentException("tMin must be >= 0", tMin);
		}
		if (tMax > 1)
		{
			throw CompGeoUtils.createIllegalArgumentException("tMax must be <= 1", tMin);
		}
		if (tMin > tMax)
		{
			throw CompGeoUtils.createIllegalArgumentException("tMin be less than tMax", tMin, tMax);
		}
		if (stepCount < 0)
		{
			throw CompGeoUtils.createIllegalArgumentException("stepCount must be > 0", stepCount);
		}
	}

	@Override
	public String toString()
	{
		return String.format("BezierCurve{controlPoints=%s}", controlPoints);
	}
}
