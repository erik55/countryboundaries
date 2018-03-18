# countryboundaries

Java library to enable fast offline reverse country geocoding: Find out the country / state in which a geo position is located.

It is well tested, does not have any dependencies, works well on Android and most importantly, is very fast.

## Copyright and License

© 2018 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).
The default data used is derived from OpenStreetMap and thus © OpenStreetMap contributors and licensed under the [Open Data Commons Open Database License](https://opendatacommons.org/licenses/odbl/) (ODbL).

## Usage

Add [`de.westnordost:countryboundaries:1.0`](https://maven-repository.com/artifact/de.westnordost/countryboundaries/1.0) as a Maven dependency or download the jar from there.

```java
	// load data
	CountryBoundaries boundaries = CountryBoundaries.load(new FileInputStream("boundaries.ser"));
	
	// get country ids
	boundaries.getIds(-96.7954, 32.7816); // returns "US-TX","US"
```

The default data file is in `/data/`. Don't forget to give attribution when distributing it.

## Speed

With the default data set, you can expect each call to take something between 0.1 to 0.5 ms - tested on my Sony Xperia Z1 Compact (from 2014). What makes it that fast is that the boundaries are split up into a raster. In the default data, I used a raster of 120x180 (= one cell is 2° in longitude, 1° in latitude).
If you need it even faster (down to below 0.1 ms), you need to import the data set into a bigger raster, see below. The bigger the raster, the larger the file, of course.

## Data

What exactly is returned when calling `getIds` is dependent on the data set used. The default data set in `/data/` is generated from [this file in the JOSM project](https://josm.openstreetmap.de/export/HEAD/josm/trunk/data/boundaries.osm). It...
- uses ISO 3166-1 alpha-2 country codes where available and otherwise ISO 3166-2 for subdivision codes. The data set includes subdivisions only for the United States, Australia, China and India.
- is oblivious of sea borders and will only return correct results for geo positions on land. If you are a pirate and want to know when you reached international waters, don't use this data!

You can import own data from a GeoJson or an OSM XML, using the Java application in the `/generator/` folder. See the source code there for details, it's not that much.