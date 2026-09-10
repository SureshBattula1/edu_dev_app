<?php
$logoPath = 'C:/xampp/htdocs/schools/MyEduApp/branding/logo_source.png';
$splashPath = 'C:/xampp/htdocs/schools/MyEduApp/branding/splash_source.png';

$base = 'C:/xampp/htdocs/schools/MyEduApp';

function loadImage(string $path) {
    $info = @getimagesize($path);
    if (!$info) {
        throw new RuntimeException("Failed to inspect $path");
    }
    $mime = $info['mime'] ?? '';
    $img = match ($mime) {
        'image/png' => imagecreatefrompng($path),
        'image/jpeg', 'image/jpg' => imagecreatefromjpeg($path),
        'image/webp' => function_exists('imagecreatefromwebp') ? imagecreatefromwebp($path) : false,
        'image/gif' => imagecreatefromgif($path),
        default => false,
    };
    if (!$img) {
        throw new RuntimeException("Failed to load $path ($mime)");
    }
    imagealphablending($img, true);
    imagesavealpha($img, true);
    return $img;
}

function ensureDir(string $dir): void {
    if (!is_dir($dir)) {
        mkdir($dir, 0777, true);
    }
}

function savePng($img, string $path): void {
    ensureDir(dirname($path));
    imagesavealpha($img, true);
    imagepng($img, $path, 6);
    echo "Wrote $path (" . imagesx($img) . "x" . imagesy($img) . ")\n";
}

/**
 * Fit source into a square canvas with padding ratio (0-1 of canvas used by content).
 */
function makeSquare($src, int $size, float $contentRatio = 0.72, bool $transparentBg = false, ?array $bgRgb = [255, 255, 255]) {
    $out = imagecreatetruecolor($size, $size);
    imagesavealpha($out, true);
    if ($transparentBg) {
        $transparent = imagecolorallocatealpha($out, 0, 0, 0, 127);
        imagefill($out, 0, 0, $transparent);
    } else {
        $bg = imagecolorallocate($out, $bgRgb[0], $bgRgb[1], $bgRgb[2]);
        imagefilledrectangle($out, 0, 0, $size, $size, $bg);
    }

    $sw = imagesx($src);
    $sh = imagesy($src);
    $max = (int) floor($size * $contentRatio);
    $scale = min($max / $sw, $max / $sh);
    $dw = max(1, (int) floor($sw * $scale));
    $dh = max(1, (int) floor($sh * $scale));
    $dx = (int) floor(($size - $dw) / 2);
    $dy = (int) floor(($size - $dh) / 2);
    imagecopyresampled($out, $src, $dx, $dy, 0, 0, $dw, $dh, $sw, $sh);
    return $out;
}

/**
 * Crop roughly the left/top logo mark (bridge) for small icons when full wordmark is too wide.
 * For horizontal logos, keep left portion that contains the bridge.
 */
function cropLogoMark($src) {
    $sw = imagesx($src);
    $sh = imagesy($src);
    // Bridge sits upper-left; take square from left/top with some padding
    $side = min($sw, (int) floor($sh * 1.35));
    $side = min($side, $sw);
    $out = imagecreatetruecolor($side, $side);
    imagesavealpha($out, true);
    $transparent = imagecolorallocatealpha($out, 0, 0, 0, 127);
    imagefill($out, 0, 0, $transparent);
    // Center vertically on source crop
    $sy = max(0, (int) floor(($sh - $side) / 2));
    if ($side > $sh) {
        $sy = 0;
        $sideH = $sh;
        $dy = (int) floor(($side - $sideH) / 2);
        imagecopy($out, $src, 0, $dy, 0, 0, $side, $sideH);
    } else {
        imagecopy($out, $src, 0, 0, 0, $sy, $side, $side);
    }
    return $out;
}

$logo = loadImage($logoPath);
$splash = loadImage($splashPath);
echo 'logo ' . imagesx($logo) . 'x' . imagesy($logo) . PHP_EOL;
echo 'splash ' . imagesx($splash) . 'x' . imagesy($splash) . PHP_EOL;

$brandDir = "$base/branding";
ensureDir($brandDir);

// Master + Play Store
savePng(makeSquare($logo, 1024, 0.78, false), "$brandDir/logo_1024.png");
savePng(makeSquare($logo, 512, 0.78, false), "$brandDir/play_store_512.png");
savePng(makeSquare($logo, 512, 0.85, true), "$brandDir/logo_512_transparent.png");
savePng(makeSquare($logo, 256, 0.85, true), "$brandDir/logo_256.png");

$mark = cropLogoMark($logo);
savePng($mark, "$brandDir/logo_mark_crop.png");
savePng(makeSquare($mark, 432, 0.70, true), "$brandDir/ic_launcher_foreground_432.png");
savePng(makeSquare($mark, 108, 0.70, true), "$brandDir/ic_launcher_foreground_108.png");

// Android mipmaps (legacy full icons on white)
$mipmaps = [
    'mipmap-mdpi' => 48,
    'mipmap-hdpi' => 72,
    'mipmap-xhdpi' => 96,
    'mipmap-xxhdpi' => 144,
    'mipmap-xxxhdpi' => 192,
];
foreach ($mipmaps as $folder => $size) {
    $dir = "$base/app/src/main/res/$folder";
    $icon = makeSquare($mark, $size, 0.78, false);
    savePng($icon, "$dir/ic_launcher.png");
    savePng($icon, "$dir/ic_launcher_round.png");
    // remove old webp so PNG wins
    foreach (['ic_launcher.webp', 'ic_launcher_round.webp'] as $old) {
        $p = "$dir/$old";
        if (file_exists($p)) {
            unlink($p);
            echo "Removed $p\n";
        }
    }
    imagedestroy($icon);
}

// Adaptive foreground drawable PNG
$fg = makeSquare($mark, 432, 0.66, true);
savePng($fg, "$base/app/src/main/res/drawable/ic_launcher_foreground.png");
imagedestroy($fg);

// White background for adaptive icon
$bg = imagecreatetruecolor(432, 432);
$white = imagecolorallocate($bg, 255, 255, 255);
imagefilledrectangle($bg, 0, 0, 432, 432, $white);
savePng($bg, "$base/app/src/main/res/drawable/ic_launcher_background.png");
imagedestroy($bg);

// Compose / shared drawables for splash + logo
$drawables = [
    'drawable-mdpi' => 1.0,
    'drawable-hdpi' => 1.5,
    'drawable-xhdpi' => 2.0,
    'drawable-xxhdpi' => 3.0,
    'drawable-xxxhdpi' => 4.0,
];

// Splash: keep aspect, scale width to ~360dp base
$splashW = imagesx($splash);
$splashH = imagesy($splash);
foreach ($drawables as $folder => $scale) {
    $dir = "$base/app/src/main/res/$folder";
    ensureDir($dir);
    $w = (int) round(360 * $scale);
    $h = (int) max(1, round($w * ($splashH / $splashW)));
    $out = imagecreatetruecolor($w, $h);
    imagesavealpha($out, true);
    $transparent = imagecolorallocatealpha($out, 0, 0, 0, 127);
    imagefill($out, 0, 0, $transparent);
    imagecopyresampled($out, $splash, 0, 0, 0, 0, $w, $h, $splashW, $splashH);
    savePng($out, "$dir/splash_brand.png");
    imagedestroy($out);

    // Full logo for in-app use (~200dp wide)
    $lw = (int) round(200 * $scale);
    $lh = (int) max(1, round($lw * (imagesy($logo) / imagesx($logo))));
    $lout = imagecreatetruecolor($lw, $lh);
    imagesavealpha($lout, true);
    $t = imagecolorallocatealpha($lout, 0, 0, 0, 127);
    imagefill($lout, 0, 0, $t);
    imagecopyresampled($lout, $logo, 0, 0, 0, 0, $lw, $lh, imagesx($logo), imagesy($logo));
    savePng($lout, "$dir/logo_bigbridz.png");
    imagedestroy($lout);
}

// Also put masters in composeApp android resources if present
$composeRes = "$base/composeApp/src/androidMain/res";
foreach ($drawables as $folder => $scale) {
    $srcSplash = "$base/app/src/main/res/$folder/splash_brand.png";
    $srcLogo = "$base/app/src/main/res/$folder/logo_bigbridz.png";
    $destDir = "$composeRes/$folder";
    ensureDir($destDir);
    if (file_exists($srcSplash)) copy($srcSplash, "$destDir/splash_brand.png");
    if (file_exists($srcLogo)) copy($srcLogo, "$destDir/logo_bigbridz.png");
}

// commonMain composeResources
$cr = "$base/composeApp/src/commonMain/composeResources/drawable";
ensureDir($cr);
copy("$brandDir/logo_512_transparent.png", "$cr/logo_bigbridz.png");
// Save a reasonably sized splash for compose
$sw = 720;
$sh = (int) max(1, round($sw * ($splashH / $splashW)));
$sout = imagecreatetruecolor($sw, $sh);
imagesavealpha($sout, true);
$t = imagecolorallocatealpha($sout, 0, 0, 0, 127);
imagefill($sout, 0, 0, $t);
imagecopyresampled($sout, $splash, 0, 0, 0, 0, $sw, $sh, $splashW, $splashH);
savePng($sout, "$cr/splash_brand.png");
imagedestroy($sout);

imagedestroy($logo);
imagedestroy($splash);
imagedestroy($mark);
echo "Done\n";
