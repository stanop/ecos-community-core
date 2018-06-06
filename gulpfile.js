let gulp = require('gulp'),
    babel = require('gulp-babel'),
    sourcemaps = require('gulp-sourcemaps'),
    rename = require('gulp-rename'),
    uglify = require('gulp-uglify'),
    cleanCSS = require('gulp-clean-css'),
    gutil = require('gulp-util');

let buildParams = {};
for (let arg of process.argv) {
    if (arg.startsWith('--')) {
        let keyValue = arg.split('=');
        if (keyValue.length == 2) {
            buildParams[keyValue[0].substring(2)] = keyValue[1];
        }
    }
}

let moduleId = buildParams['artifactId'];
let skipCompression = buildParams['skipCompression'] == 'true';

let webRoot = moduleId + "/target/classes/META-INF/";
let source = {
    jsx: [
        webRoot + '**/*.jsx'
    ],
    js: [
        webRoot + '**/*.js',
        '!' + webRoot + 'js/**',
        '!' + webRoot + '**/*min.js',
    ],
    babelJs: [
        webRoot + 'js/**/*.js',
        '!' + webRoot + '**/*min.js',
    ],
    css: [
        webRoot + "**/*.css",
        '!' + webRoot + "**/*min.css"
    ]
};

function processMinResources(paths, processors) {

    let stream = gulp.src(paths)
        .pipe(sourcemaps.init());

    for (let proc of processors) {
        stream = stream.pipe(proc);
    }

    return stream
        .pipe(rename(function (path) {
            path.basename += ".min";
        }))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(function(file) {
            return file.base;
        }));
}

gulp.task('process-css', function() {
    return processMinResources(source.css, [
        skipCompression ? gutil.noop() : cleanCSS({
            inline: false,
            rebase: false
        })
    ]);
});

gulp.task('process-jsx', function() {
    return processMinResources(source.jsx, [
        babel({
            compact: false,
            presets: [
                ['env', {
                    modules: 'amd'
                }],
                'react'
            ]
        }),
        skipCompression ? gutil.noop() : uglify()
    ]);
});

gulp.task('process-babel-js', function() {
    return processMinResources(source.babelJs, [
        babel({
            compact: false,
            presets: [
                ['env', {
                    modules: false
                }]
            ]
        }),
        skipCompression ? gutil.noop() : uglify()
    ]);
});

gulp.task('process-js', function() {
    return processMinResources(source.js, [
        skipCompression ? gutil.noop() : uglify()
    ]);
});

gulp.task('default', ['process-css', 'process-jsx', 'process-js', 'process-babel-js']);
