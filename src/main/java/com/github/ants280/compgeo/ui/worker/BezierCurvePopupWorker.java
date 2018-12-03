package com.github.ants280.compgeo.ui.worker;

import com.github.ants280.compgeo.Point;
import com.github.ants280.compgeo.algorithm.BezierCurve;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JFrame;

public class BezierCurvePopupWorker extends CompGeoPopupWorker<List<Point>>
{
	private final BezierCurve bezierCurve;
	private static final int POINTS_PER_CALCULATION = 10;
	// It might be useful to recalculate with higher precision if the points are only slightly past the max difference.
	private static final Double MAX_POINT_DIFFERENCE = 0.5d;

	public BezierCurvePopupWorker(
			Consumer<List<Point>> completedAction,
			JFrame frame,
			List<Point> points)
	{
		super(completedAction, frame, "Bezier Curve being created...");
		this.bezierCurve = new BezierCurve(points);
	}

	@Override
	protected List<Point> doInBackground()
	{
		return getBezierCurvePoints(0d, 1d, 1d);
	}

	private List<Point> getBezierCurvePoints(double tMin, double tMax, double calculationPercentage)
	{
		List<Point> points = bezierCurve.getPoints(tMin, tMax, POINTS_PER_CALCULATION, MAX_POINT_DIFFERENCE);
		if (points != null)
		{
			this.updateProgress(calculationPercentage);

			return points;
		}

		points = new ArrayList<>(POINTS_PER_CALCULATION * 2);
		double tMid = (tMin + tMax) / 2d;
		List<Point> leftPoints = this.getBezierCurvePoints(
				tMin,
				tMid,
				calculationPercentage / 2d);
		List<Point> rightPoints = this.getBezierCurvePoints(
				tMid,
				tMax,
				calculationPercentage / 2d);

		assert leftPoints.get(leftPoints.size() - 1)
				.equals(rightPoints.get(0));
		rightPoints = rightPoints.subList(1, rightPoints.size());

		points.addAll(leftPoints);
		points.addAll(rightPoints);
		return points;
	}
}
