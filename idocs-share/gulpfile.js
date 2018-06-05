let gulp = require('gulp'),
    babel = require('gulp-babel'),
    sourcemaps = require('gulp-sourcemaps'),
    rename = require('gulp-rename'),
    uglify = require('gulp-uglify'),
    cleanCSS = require('gulp-clean-css');

let webRoot = "target/classes/META-INF/";
let source = {
    jsx: [
        webRoot + '**/*.jsx'
    ],
    js: [
        webRoot + '**/*.js',
        '!' + webRoot + '**/*min.js',
    ],
    css: [
        webRoot + "**/*.css",
        '!' + webRoot + "**/*min.css"
    ]
};

gulp.task('process-css', function() {
    return gulp.src(source.css)
        .pipe(sourcemaps.init())
        .pipe(cleanCSS({
            inline: false,
            rebase: false
        }))
        .pipe(rename(function (path) {
            path.basename += "-min";
        }))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(function(file) {
            return file.base;
        }));
});

gulp.task('process-jsx', function() {
    return gulp.src(source.jsx)
        .pipe(sourcemaps.init())
        .pipe(babel({
            compact: true,
            comments: false,
            plugins: ["transform-es2015-modules-amd"]
        }))
        .pipe(rename(function (path) {
            path.basename += "-min";
        }))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(function(file) {
            return file.base;
        }));
});

gulp.task('process-js', function() {
    return gulp.src(source.js)
        .pipe(sourcemaps.init())
        .pipe(babel({
            compact: false
        }))
        .pipe(uglify())
        .pipe(rename(function (path) {
            path.basename += "-min";
        }))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(function(file) {
            return file.base;
        }));
});

gulp.task('default', ['process-css', 'process-jsx', 'process-js']);
