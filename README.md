<img src="https://www.huber.embl.de/courses/images/BioIT_logo.jpeg" width="200">

# BigDataProcessor

Interactive processing of TB-sized image data within Fiji, on your laptop.

### Interactive cropping

![bdp2-interactive-crop-of-binned-view-670gb](https://user-images.githubusercontent.com/2157566/54450529-e10a1180-4750-11e9-85c8-ecebe936710d.gif)

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
