<img src="https://www.huber.embl.de/courses/images/BioIT_logo.jpeg" width="200">

# BigDataProcessor2

Interactive processing of TB-sized image data within [Fiji](http://fiji.sc/), on your laptop.

## Overview and examples

### Opening TB-sized Tiff or Hdf5 based raw data

Below movie demonstrates opening of 670 GB Hdf5 image data, acquired by light-sheet microscopy. 
The data format is one volumetric Hdf5 file per time-point. As the data is needed to be streamed from the camera onto a disk efficiently, the Hdf5 files are neither specifically chunked for efficient 3D access nor do they contain a multi-resolution pyramid. 
Also, there is no special header file linking the different time-points. Nevertheless, as the movie shows  *(click below to play)*, 
the data can be opened and browsed within seconds. 

[<img width="1000" alt="image" src="./docs/images/1.png">](https://drive.google.com/open?id=1wJgZb_Hd1S8ScTbKq9KOYyz-4lmS7-Sf)

### Interactive binning

For camera based microscopy systems the pixel size cannot be freely chosen during acquisition. 
Thus, the user is often forced to over-sample in order not to lose important information. 
However, this leads to large data volumes with noise since the information is spread across many pixels and therefore resulting in 
(vastly) increased image processing times.  
The BigDataProcessor makes it possible to explore different binnings interactively, thereby providing an efficient means to 
attain a binning at which the corresponding scientific question can be efficiently addressed.*(click below to play the movie)*

[<img width="1000" alt="image" src="./docs/images/2.png">](https://drive.google.com/open?id=1AVFW3M5QYEDH9XUgR-q2LWUsuy16zF1A)

### Interactive 3D+t Cropping

Often, large volumes are acquired than required, e.g. to accommodate sample drift, motion, or growth. 
Below movie shows *(click below to play)* how the BigDataProcessor can be interactively used to crop the data to only contain the relevant parts.

[<img width="1000" alt="image" src="./docs/images/3.png">](https://drive.google.com/open?id=1iabVP9jbISI1WclMRjtDHvcNWxMTC95-)

### Interactive conversion to 8-bit

Cameras typically produce image data at 12, 14, or 16 bit-depths (12 and 14 bit are not common data formats they are often stored as 16 bit anyway). 
For many image analysis tasks, 8-bit depth is usually sufficient affording the user to reduce data size as well as increase processing time by a factor of 2.
However, converting 16-bit to 8-bit data can be tricky as it entails deciding on a specific mapping from the higher to the lower bit-depth. 
Choosing a mapping of 65535 => 255 and 0 => 0 can lead to a low dynamic range in the 8-bit range especially when the input contains 
only a subset of the full 16-bit range. Furthermore, choosing `max` => 255 and `min` => 0, can be sub-optimal if 
there are spurious pixels with very high values, making the `max` value very high, again leading to a low dynamic range for the relevant grey values 
in the 8-bit converted data.  
The below movie *(click below to play)* demonstrates interactive 8-bit conversion, where the user can interactively explore different mappings while browsing the entire data. 
This is done lazily, i.e. the data on disk is not altered at this stage.

[<img width="1000" alt="image" src="./docs/images/4.png">](https://drive.google.com/open?id=1jRZEepD1C8rM5t2gDi7tYnFh092vUztm)

## Detailed information

**BigDataProcessor** is an [ImageJ](https://imagej.net) plugin designed for inspection, manipulation and conversion of big data image formats flawlessly even on a basic laptop or a computer.
The plugin implements a [Lazy Loading design pattern](https://en.wikipedia.org/wiki/Lazy_loading) to seamlessly render Terabyte sized big data image data produced by light-sheet and electron microscopy, without bothering the RAM capacity. 
The plugin facilitates loading & re-saving of TIF, HDF5 and Imaris HDF5 formats meanwhile allowing the user to shear, crop or bin. *(check out the User Documentation section below to explore what more you can do!)*

The plugin also encloses **BigDataTracker**, an object tracker tool for the big data images and also allowing to view and save just the tracked regions.
The BigDataProcessor harnesses the power of the popular [BigDataViewer](https://imagej.net/BigDataViewer) to render and [ImgLib2](https://imagej.net/ImgLib2) library for image processing towards an efficient plugin software suitable for everyday use for all microscopy practitioners.

## History
The BigDataProcessor is a new revamped version of the popular [BigDataTools](https://github.com/tischi/fiji-plugin-bigDataTools2). The BigDataProcessor is developed almost from scratch using ImgLib2 framework while preserving crux the BigDataTools. 
The new plugin uses Big Data Viewer for flexible and efficient rendering however the backward compatibility with ImageJ1 viewer is a work in progress! Keep Posted!

## Supported Formats

- Multi-stack TIFF
- Single plane TIFF
- Multi-stack HDF
- Multi-stack IMARIS HDF

## Export Options

- Binning
- 2D Projections of stacks
- Gating pixels
- 4D Cropping
 
## Installation
Within ImageJ/Fiji you can install the plugin using the `Help > Update > Manage Update Sites` and select the `EMBL-CBA` site.

Note: The plugins need Java 1.8 if you see error messages popping up that might be caused by an older Java version.

## Contributors

Ashis Ravindran&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;Christian Tischer  
ashis.r91@gmail.com&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;christian.tischer@embl.de

## User Documentation

*Coming soon*
