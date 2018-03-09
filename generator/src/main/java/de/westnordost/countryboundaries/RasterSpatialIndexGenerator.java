package de.westnordost.countryboundaries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO test
public class RasterSpatialIndexGenerator
{
	public RasterSpatialIndex generate(int width, int height, CountryBoundaries boundaries)
	{
		ArrayList<QueryResult> indexToCountryMap = new ArrayList<>();
		Map<QueryResult, Short> countryToIndexMap = new HashMap<>();

		short[] raster = new short[width * height];

		for(int y=0; y < height; ++y)
		{
			for(int x=0; x < width; ++x)
			{
				double lonMin = -180.0 + 360.0 * x/width;
				double latMin = +90.0 - 180.0 * y/height;
				double lonMax = -180.0 + 360.0 * (x+1)/width;
				double latMax = +90.0 - 180.0 * (y+1)/height;

				QueryResult box = boundaries.getIds(lonMin, latMin, lonMax, latMax);
				short index;
				// already index assigned
				if(countryToIndexMap.containsKey(box))
				{
					index = countryToIndexMap.get(box);
				}
				// no index assigned yet
				else
				{
					indexToCountryMap.add(box);
					index = (short) (indexToCountryMap.size()-1);
					countryToIndexMap.put(box, index);
				}

				raster[x + y * width] = index;
			}
		}

		QueryResult[] indices = new QueryResult[indexToCountryMap.size()];
		indexToCountryMap.toArray(indices);

		return new RasterSpatialIndex(raster, width, indices);
	}
}