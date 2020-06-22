# BigDataProcessor2

Interactive processing of TB-sized image data within [Fiji](http://fiji.sc/), on your laptop.

## Install

BigDataProcessor2 is a Fiji plugin and can be installed via an update site.

- Please install [Fiji](fiji.sc)
    - To avoid dependency issues we currently recommended to download a fresh Fiji (you can have multiple versions of Fiji on one computer, e.g. you could rename this one from 'Fiji.app' to 'Fiji-BDP2.app')
- Within Fiji, please enable the following [Update Site](https://imagej.net/Update_Sites): 
    - [X] BigDataProcessor2
    - (Note: The EMBL-CBA update site one must currently **not** be checked because of dependency issues!)
- Restart Fiji

## Start

In the Fiji Plugins menu scroll far down and start BigDataProcessor2:

<img src="https://user-images.githubusercontent.com/2157566/83620048-0d47c300-a58d-11ea-8213-306598048e9e.png" width="460">

This opens up an empty BigDataViewer window with additional menu entries.

Note: **Don't use the Commands**, they just serve the macro recording.

## Record macro

All actions can be recorded as ImageJ macros. To enable this please:

<img src="https://user-images.githubusercontent.com/2157566/85285145-abde8a00-b490-11ea-9fca-8661c87bc261.png" width="350">
<img src="https://user-images.githubusercontent.com/2157566/85285194-bc8f0000-b490-11ea-9a63-f52543d1a714.png" width="200">

Note: This is equivalent to [ Plugins > Macros > Record... ] in the ImageJ menu, we just thought it is more convenient to also have it in the BigDataProcessor2 menu tree.

## Open dataset

In the BigDataViewer window, go to the menu bar and use the menu items in [ BigDataProcessor2 > Open ].

<img src="https://user-images.githubusercontent.com/2157566/85284864-3377c900-b490-11ea-9a02-83056f792c14.png" width="460">

## Process

### Bin

For camera based microscopy systems the pixel size cannot be freely chosen during acquisition. 
Thus, the user is often forced to over-sample in order not to lose important information. 
However, this leads to large data volumes with noise since the information is spread across many pixels and therefore resulting in 
(vastly) increased image processing times.  
The BigDataProcessor2 makes it possible to develop different binnings interactively, thereby providing an efficient means to 
attain a binning at which the corresponding scientific question can be efficiently addressed.*(click below to play the movie)*

[<img width="1000" alt="image" src="./docs/images/2.png">](https://drive.google.com/open?id=1AVFW3M5QYEDH9XUgR-q2LWUsuy16zF1A)

### Crop

Often, large volumes are acquired than required, e.g. to accommodate sample drift, motion, or growth. 
Below movie shows *(click below to play)* how the BigDataProcessor2 can be interactively used to crop the data to only contain the relevant parts.

[<img width="1000" alt="image" src="./docs/images/3.png">](https://drive.google.com/open?id=1iabVP9jbISI1WclMRjtDHvcNWxMTC95-)

### Convert to 8-bit

Cameras typically produce image data at 12, 14, or 16 bit-depths (12 and 14 bit are not common data formats they are often stored as 16 bit anyway). 
For many image analysis tasks, 8-bit depth is usually sufficient affording the user to reduce data size as well as increase processing time by a factor of 2.
However, converting 16-bit to 8-bit data can be tricky as it entails deciding on a specific mapping from the higher to the lower bit-depth. 
Choosing a mapping of 65535 => 255 and 0 => 0 can lead to a low dynamic range in the 8-bit range especially when the input contains 
only a subset of the full 16-bit range. Furthermore, choosing `max` => 255 and `min` => 0, can be sub-optimal if 
there are spurious pixels with very high values, making the `max` value very high, again leading to a low dynamic range for the relevant grey values 
in the 8-bit converted data.  
The below movie *(click below to play)* demonstrates interactive 8-bit conversion, where the user can interactively develop different mappings while browsing the entire data. 
This is done lazily, i.e. the data on disk is not altered at this stage.

[<img width="1000" alt="image" src="./docs/images/4.png">](https://drive.google.com/open?id=1jRZEepD1C8rM5t2gDi7tYnFh092vUztm)

## Save

TODO...

## Additional information

**BigDataProcessor2 (BDP2)**  is an [ImageJ](https://imagej.net) plugin designed for inspection, manipulation and conversion of big data image formats even on a basic laptop or a computer.

BigDataProcessor2 is based on [BigDataViewer](https://imagej.net/BigDataViewer) for rendering and the [ImgLib2](https://imagej.net/ImgLib2) library for image processing. 

BigDataProcessor2 implements a [Lazy Loading design pattern](https://en.wikipedia.org/wiki/Lazy_loading) to render Terabyte sized big data image data produced by light-sheet and electron microscopy, also on laptops with limited RAM. 

The plugin facilitates loading & saving of TIFF, HDF5 and Imaris file formats meanwhile allowing the user to shear, crop or bin. *(check out the User Documentation section below to develop what more you can do!)*

The plugin also encloses **BigDataTracker**, an object tracker tool for the big data images and also allowing to view and save just the tracked regions.

### History

The BigDataProcessor2 is a new version of the **BigDataTools** plugin, a.k.a [BigDataProcessor](https://github.com/embl-cba/fiji-plugin-bigDataProcessor). BigDataProcessor2 is developed almost from scratch using the ImgLib2 framework.

### Supported Formats

- Multi-stack TIFF
- Single plane TIFF
- Multi-stack HDF
- Multi-stack IMARIS HDF

### Contributors

Ashis Ravindran&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;Christian Tischer  
ashis.r91@gmail.com&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;christian.tischer@embl.de
