/* global proj4 */
proj4.defs([
    // ETRS-TM35FIN
    ['EPSG:3067', '+proj=utm +zone=35 +ellps=GRS80 +units=m +no_defs'],
    // WGS84
    ['EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs'],
    // KKJ
    ['EPSG:2393', '+proj=tmerc +lat_0=0 +lon_0=27 +k=1 +x_0=3500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7435,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs']
]);

L.CRS.EPSG3067 = new L.Proj.CRS('EPSG:3067',
    '+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs',
    {
        tms: true,
        origin: [-548576, 6291456],
        bounds: L.bounds([-548576, 6291456], [1548576, 8388608]),
        resolutions: [
            8192, 4096, 2048, 1024, 512, 256,
            128, 64, 32, 16, 8, 4, 2, 1, 0.5,
            0.25, 0.125, 0.0625, 0.03125, 0.015625
        ]
    }
);

// Temporary fix for L.Scale control.
// see https://github.com/kartena/Proj4Leaflet/issues/109
L.CRS.EPSG3067.distance = L.CRS.Earth.distance;
L.CRS.EPSG3067.R = 6378137;
