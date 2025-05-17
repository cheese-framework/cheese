var console_log = console.log;
console.log = function (...args) {
    function captureStackTrace() {
        var obj = {};
        Error.captureStackTrace(obj, captureStackTrace);
        return obj.stack;
    }
    const stack = captureStackTrace().trim().split('\n').slice(1).map(line => line.trim()).filter(line => line !== "");
    let prefix = "unknown location:";  // 默认情况
    let filePath ;  // 默认情况
    let lineNumber;  // 默认情况
    const regex = /at (.+):(\d+)/;
    const match = stack[0].match(regex);
    if (stack.length > 0) {
        if (match) {
             filePath = match[1].replace(/.*?(\/js\/.+)/, '$1');
             lineNumber = match[2];
            prefix = `(${filePath}:${lineNumber}):`;
        } else {
            const fallbackRegex = /at (.+):(\d+):\d+/;
            const fallbackMatch = stack[0].match(fallbackRegex);
            if (fallbackMatch) {
                 filePath = fallbackMatch[1];
                 lineNumber = fallbackMatch[2];
                prefix = `(${filePath}:${lineNumber}):`;
            } else {
                prefix = "unknown location:";
            }
        }
    }
    let c=net.codeocean.cheese.core.utils.ScriptLogger
    const logMessage = [prefix].concat(args).join(' ');
    c.INSTANCE.i(logMessage)
};

var console_error = console.error;
console.error = function (...args) {
    function captureStackTrace() {
        var obj = {};
        Error.captureStackTrace(obj, captureStackTrace);
        return obj.stack;
    }
    const stack = captureStackTrace().trim().split('\n').slice(1).map(line => line.trim()).filter(line => line !== "");
    let prefix = "unknown location:";  // 默认情况
    let filePath ;  // 默认情况
    let lineNumber;  // 默认情况
    const regex = /at (.+):(\d+)/;
    const match = stack[0].match(regex);
    if (stack.length > 0) {
        if (match) {
             filePath = match[1].replace(/.*?(\/js\/.+)/, '$1');
             lineNumber = match[2];
            prefix = `(${filePath}:${lineNumber}):`;
        } else {
            const fallbackRegex = /at (.+):(\d+):\d+/;
            const fallbackMatch = stack[0].match(fallbackRegex);
            if (fallbackMatch) {
                 filePath = fallbackMatch[1];
                 lineNumber = fallbackMatch[2];
                prefix = `(${filePath}:${lineNumber}):`;
            } else {
                prefix = "unknown location:";
            }
        }
    }
    let c=net.codeocean.cheese.core.utils.ScriptLogger
    const logMessage = [prefix].concat(args).join(' ');
    c.INSTANCE.e(logMessage)
};


var console_warn = console.warn;
console.warn = function (...args) {
    function captureStackTrace() {
        var obj = {};
        Error.captureStackTrace(obj, captureStackTrace);
        return obj.stack;
    }
    const stack = captureStackTrace().trim().split('\n').slice(1).map(line => line.trim()).filter(line => line !== "");
    let prefix = "unknown location:";  // 默认情况
    let filePath ;  // 默认情况
    let lineNumber;  // 默认情况
    const regex = /at (.+):(\d+)/;
    const match = stack[0].match(regex);
    if (stack.length > 0) {
        if (match) {
             filePath = match[1].replace(/.*?(\/js\/.+)/, '$1');
             lineNumber = match[2];
            prefix = `(${filePath}:${lineNumber}):`;
        } else {
            const fallbackRegex = /at (.+):(\d+):\d+/;
            const fallbackMatch = stack[0].match(fallbackRegex);
            if (fallbackMatch) {
                 filePath = fallbackMatch[1];
                 lineNumber = fallbackMatch[2];
                prefix = `(${filePath}:${lineNumber}):`;
            } else {
                prefix = "unknown location:";
            }
        }
    }
    let c=net.codeocean.cheese.core.utils.ScriptLogger
    const logMessage = [prefix].concat(args).join(' ');
    c.INSTANCE.w(logMessage)
};

var console_info = console.info;
console.info = function (...args) {
    function captureStackTrace() {
        var obj = {};
        Error.captureStackTrace(obj, captureStackTrace);
        return obj.stack;
    }
    const stack = captureStackTrace().trim().split('\n').slice(1).map(line => line.trim()).filter(line => line !== "");
    let prefix = "unknown location:";  // 默认情况
    let filePath ;  // 默认情况
    let lineNumber;  // 默认情况
    const regex = /at (.+):(\d+)/;
    const match = stack[0].match(regex);
    if (stack.length > 0) {
        if (match) {
             filePath = match[1].replace(/.*?(\/js\/.+)/, '$1');
             lineNumber = match[2];
            prefix = `(${filePath}:${lineNumber}):`;
        } else {
            const fallbackRegex = /at (.+):(\d+):\d+/;
            const fallbackMatch = stack[0].match(fallbackRegex);
            if (fallbackMatch) {
                 filePath = fallbackMatch[1];
                 lineNumber = fallbackMatch[2];
                prefix = `(${filePath}:${lineNumber}):`;
            } else {
                prefix = "unknown location:";
            }
        }
    }
    let c=net.codeocean.cheese.core.utils.ScriptLogger
    const logMessage = [prefix].concat(args).join(' ');
    c.INSTANCE.i(logMessage)
};

var console_debug = console.debug;
console.debug = function (...args) {
    function captureStackTrace() {
        var obj = {};
        Error.captureStackTrace(obj, captureStackTrace);
        return obj.stack;
    }
    const stack = captureStackTrace().trim().split('\n').slice(1).map(line => line.trim()).filter(line => line !== "");
    let prefix = "unknown location:";  // 默认情况
    let filePath ;  // 默认情况
    let lineNumber;  // 默认情况
    const regex = /at (.+):(\d+)/;
    const match = stack[0].match(regex);
    if (stack.length > 0) {
        if (match) {
             filePath = match[1].replace(/.*?(\/js\/.+)/, '$1');
             lineNumber = match[2];
            prefix = `(${filePath}:${lineNumber}):`;
        } else {
            const fallbackRegex = /at (.+):(\d+):\d+/;
            const fallbackMatch = stack[0].match(fallbackRegex);
            if (fallbackMatch) {
                 filePath = fallbackMatch[1];
                 lineNumber = fallbackMatch[2];
                prefix = `(${filePath}:${lineNumber}):`;
            } else {
                prefix = "unknown location:";
            }
        }
    }
    let c=net.codeocean.cheese.core.utils.ScriptLogger
    const logMessage = [prefix].concat(args).join(' ');
    c.INSTANCE.d(logMessage)
};