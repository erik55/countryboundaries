package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO TEST
public class CountryBoundaries
{
	private static final int WGS84 = 4326;

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);
	private final Map<String, Geometry> geometriesByIds;
	private final SpatialIndex spatialIndex;
	private final Map<String, Double> geometrySizeCache;

	public CountryBoundaries(GeometryCollection boundaries, SpatialIndex index)
	{
		this.spatialIndex = index;
		geometrySizeCache = new HashMap<>(400);
		geometriesByIds = new HashMap<>(400);

		for (int i = 0; i < boundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = boundaries.getGeometryN(i);

			Object userData = countryBoundary.getUserData();
			if(userData != null && userData instanceof String)
			{
				geometriesByIds.put((String) userData, countryBoundary);
			}
		}
	}

	public List<String> getIds(double longitude, double latitude)
	{
		QueryResult queryResult = spatialIndex.query(longitude, latitude);

		List<String> result = new ArrayList<>();
		result.addAll(queryResult.getContainingIds());

		Collection<String> possibleMatches = queryResult.getIntersectingIds();
		if (!possibleMatches.isEmpty())
		{
			Coordinate coord = new Coordinate(longitude, latitude, 0);
			Point point = factory.createPoint(coord);

			for (String countryCode : possibleMatches)
			{
				Geometry country = geometriesByIds.get(countryCode);
				if (country != null && country.covers(point))
				{
					result.add(countryCode);
				}
			}
		}
		Collections.sort(result, this::compareSize);
		return result;
	}

	public QueryResult getIds(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
	{
		QueryResult queryResult = spatialIndex.query(minLongitude, minLatitude, maxLongitude, maxLatitude);

		List<String> containingCountryCodes = new ArrayList<>();
		List<String> intersectingCountryCodes = new ArrayList<>();
		containingCountryCodes.addAll(queryResult.getContainingIds());

		Collection<String> possibleMatches = queryResult.getIntersectingIds();
		if (!possibleMatches.isEmpty())
		{
			Polygon box = createBounds(minLongitude, minLatitude, maxLongitude, maxLatitude);
			for (String countryCode : possibleMatches)
			{
				Geometry country = geometriesByIds.get(countryCode);
				if (country != null)
				{
					IntersectionMatrix im = country.relate(box);
					if (im.isCovers())
					{
						containingCountryCodes.add(countryCode);
					} else if (!im.isDisjoint())
					{
						intersectingCountryCodes.add(countryCode);
					}
				}
			}
		}
		Collections.sort(containingCountryCodes, this::compareSize);
		Collections.sort(intersectingCountryCodes, this::compareSize);

		return new QueryResult(containingCountryCodes, intersectingCountryCodes);
	}

	private double getSize(String isoCode)
	{
		if (!geometrySizeCache.containsKey(isoCode))
		{
			Geometry country = geometriesByIds.get(isoCode);
			if (country == null) return 0;
			geometrySizeCache.put(isoCode, country.getArea());
		}
		return geometrySizeCache.get(isoCode);
	}

	private int compareSize(String isoCode1, String isoCode2)
	{
		return (int) (getSize(isoCode1) - getSize(isoCode2));
	}

	private Polygon createBounds(double minLong, double minLat, double matLong, double maxLat)
	{
		return factory.createPolygon(new Coordinate[]
				{
						new Coordinate(minLong, minLat),
						new Coordinate(matLong, minLat),
						new Coordinate(matLong, maxLat),
						new Coordinate(minLong, maxLat),
						new Coordinate(minLong, minLat)
				});
	}
}