Known issues:

- _Gallery blocks require a WordPress.com blog, or WordPress 5.4.2+ for self-hosted blogs._
- _Quotes (e.g. `it's`) in image attributes—caption, description, alt—appear as escaped (`it\'s`) on self-hosted blogs. This is a [WordPress/Jetpack bug](https://github.com/Automattic/jetpack/issues/6119)._

----

#### 10 Aug 2020

#### UPDATES

- Landscape view layouts for relevant screens
    - new post screen
    - image attributes screen

- Multiple image uploads on self-hosted blogs
    - Upload images one-by-one for jetpack blogs, because bulk upload occasionally returns errors

&nbsp;

`version: 0.12.29 (alpha)`

---


#### 8 Aug 2020

#### New: Set post category

- Set categories for posts from post settings screen:
    - Select one of more categories from list. 
    - Add new categories by typing name and selecting `Create new...`
        - Creating child categories is not yet supported.

- Set default categories for all posts from settings screen

##### Update

Icon colour changed to light pink for the summer.

&nbsp;

`version: 0.12.26 (alpha)`

---


#### 6 Aug 2020

#### New: Create gallery posts

Select multiple images to create a gallery post. Gallery posts are supported for blogs using both, the new block editor and the classic WordPress editor.

Features:
- Tap on any image in a gallery to open its image attributes screen—set caption, mark as featured image, etc.
- Tap on add more button (bottom right corner of gallery) to add more images to the gallery
- Tap on change photos button (bottom left corner of gallery) to replace all images in the gallery
- Set gallery caption (block editor) or post caption (classic editor)
- Featured image is indicated with a &star;
- Trimmed image captions are shown
- Number of columns is calculated automatically based on number of images:
    - 2 columns for 2 or 4 images, 
    - 3 columns for 3, 5 or more images.
- Tap on reorder images button (top right corner of gallery) to change order of images

&nbsp;

#### New: Reorder images screen

Select the order of images in gallery.

- Tap an image to open in full view
- Long press and drag image to change its order

&nbsp;

#### Update: Image attributes screen

- Show image thumbnail
    - Tap to open image in full screen view
- Mark/unmark image as favourite
- Remove image from post

&nbsp;

#### Other updates

- Delete images from app storage after successfully uploading to server
- Bulk upload images for faster publishing (only works on WordPress.com blogs)
- Replaced floating new post button with combo new post and post settings button
- Background code updates

&nbsp;

`version: 0.12.21 (alpha)`

---


#### 30 Jul 2020

#### UPDATES

- Added update notes (this screen)
- Background code updates

&nbsp;

`version: 0.11 (alpha)`

---


