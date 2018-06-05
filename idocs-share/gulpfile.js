let gulp = require('gulp'),
    babel = require('gulp-babel'),
    sourcemaps = require('gulp-sourcemaps'),
    rename = require('gulp-rename'),
    cleanCSS = require('gulp-clean-css');

let webRoot = "target/classes/META-INF/";
let source = {
    js: [
        webRoot + '**/*.js',
        '!' + webRoot+ '**/*-min.js',
        webRoot+ '**/*.jsx'
    ],
    css: [
        webRoot + "**/*.css",
        '!' + webRoot + "**/*-min.css"
    ]
};

gulp.task('process-css', function() {
    return gulp.src(source.css)
        .pipe(sourcemaps.init())
        .pipe(cleanCSS({
            inline: false
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
            compact: true,
            comments: false
        }))
        .pipe(rename(function (path) {
            path.basename += "-min";
        }))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(function(file) {
            return file.base;
        }));
});

gulp.task('default', ['process-css', 'process-js']);
