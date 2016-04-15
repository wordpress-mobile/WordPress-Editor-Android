function Util () {}

Util.buildOpeningTag = function(tagName) {
    return '<' + tagName + '>';
};

Util.buildClosingTag = function(tagName) {
    return '</' + tagName + '>';
};

Util.wrapHtmlInTag = function(html, tagName) {
    return Util.buildOpeningTag(tagName) + html + Util.buildClosingTag(tagName);
};