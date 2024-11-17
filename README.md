# Template Matching by PSO Algorithm for Global Localization
Particle Swarm Optimization algorithm is utilized for global localization. Global localization is a step in SLAM applications where the current location is found in the global map when there is no prior location information. A local map, acquired from a single rotation of a LIDAR sensor, is utilized to locate the sensor's position within the existing, larger map.

  #### Input:
![genMap-blank](https://github.com/user-attachments/assets/4836fdf1-5691-4348-a575-7a57a95e320a)
  #### Result:
![genMap](https://github.com/user-attachments/assets/355fd301-228a-48bd-b683-a9c1b08040e2)

Particle Swarm Optimization (PSO) algorithm is used to optimize the number of matching operations between the local map and the global map, which is performed to locate the position with the highest similarity. The number of iterations has been successfully reduced from 363,014 (using the brute-force method) down to 3,750.

## Utilized Tools
- "Processing" v4.3 (core, video): https://processing.org
- Particle swarm optimization implementation derived from: https://github.com/therealmanalu/pso-example-java

## Notes
- ".map" files are a type of ROS. Each .map file contains a byte array of integer values which forms a 2D map. The array is organized in row-major order. The files contain 2D map information at a constant 2048x2048 pixel resolution.
- This work was conducted for a thesis at Dokuz Eylül University - Computer Engineering Department in 2017.
