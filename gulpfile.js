const gulp = require('gulp');
const markdown = require('gulp-markdown');
const concat = require('gulp-concat');
const decomment = require('gulp-decomment');

gulp.task('updateNotesMarkdown', gulp.series(() => {
    return gulp.src('Others/md/updatenotes.md')
        // Remove any comments
        .pipe(decomment.html())
        // Convert markdown to html
        .pipe(markdown())
        .pipe(gulp.dest("Others/md/out"));
}));

gulp.task('justUpdateNotes', gulp.series('updateNotesMarkdown', () => {
    return gulp.src(['Others/md/header.html', 'Others/md/out/updatenotes.html', 'Others/md/footer.html'])
        .pipe(concat("updatenotes.html"))
        .pipe(gulp.dest("app/src/main/res/raw"));
}));

gulp.task('default', gulp.series('justUpdateNotes', () => {
    gulp.watch('Others/md/*.md', gulp.series('justUpdateNotes'));
}));
