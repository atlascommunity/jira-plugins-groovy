'use strict';

const autoprefixer = require('autoprefixer');
const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const ManifestPlugin = require('webpack-manifest-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const eslintFormatter = require('react-dev-utils/eslintFormatter');
const ModuleScopePlugin = require('react-dev-utils/ModuleScopePlugin');
const paths = require('./paths');
const getClientEnvironment = require('./env');

// Webpack uses `publicPath` to determine where the app is being served from.
// It requires a trailing slash, or the file assets will get an incorrect path.
const publicPath = paths.servedPath;
// Some apps do not use client-side routing with pushState.
// For these, "homepage" can be set to "." to enable relative asset paths.
const shouldUseRelativeAssetPaths = publicPath === './';
// `publicUrl` is just like `publicPath`, but we will provide it to our app
// as %PUBLIC_URL% in `index.html` and `process.env.PUBLIC_URL` in JavaScript.
// Omit trailing slash as %PUBLIC_URL%/xyz looks better than %PUBLIC_URL%xyz.
const publicUrl = publicPath.slice(0, -1);
// Get environment variables to inject into our app.
const env = getClientEnvironment(publicUrl);

// Note: defined here because it will be used more than once.
const cssFilename = '../../src/main/resources/ru/mail/jira/plugins/groovy/css/[name].css';

// ExtractTextPlugin expects the build output to be flat.
// (See https://github.com/webpack-contrib/extract-text-webpack-plugin/issues/27)
// However, our output is structured with css, js and media folders.
// To have this structure working with relative paths, we have to use custom options.
const extractTextPluginOptions = shouldUseRelativeAssetPaths
    ? // Making sure that the publicPath goes back to to build folder.
    {publicPath: Array(cssFilename.split('/').length).join('../')}
    : {};


let watch = true;
let uglify = [];
let bundle = [];

// Source maps are resource heavy and can cause out of memory issue for large source files.
const shouldUseSourceMap = process.env.GENERATE_SOURCEMAP !== 'false';

if (process.env.NODE_ENV === 'production') {
    uglify = [
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                warnings: false,
                // Disabled because of an issue with Uglify breaking seemingly valid code:
                // https://github.com/facebookincubator/create-react-app/issues/2376
                // Pending further investigation:
                // https://github.com/mishoo/UglifyJS2/issues/2011
                comparisons: false,
            },
            output: {
                comments: false,
                // Turned on because emoji and regex is not minified properly using default
                // https://github.com/facebookincubator/create-react-app/issues/2488
                ascii_only: true,
            },
            sourceMap: shouldUseSourceMap, //todo
        })
    ];

    watch = false;
}

console.log(`sourcemap: ${shouldUseSourceMap}`);

if (process.env.ANALYZE) {
    bundle = [
        new BundleAnalyzerPlugin()
    ];
}

// This is the production configuration.
// It compiles slowly and is focused on producing a fast and minimal bundle.
// The development configuration is different and lives in a separate file.
module.exports = {
    // Don't attempt to continue if there are any errors.
    bail: true,
    // We generate sourcemaps in production. This is slow but gives good results.
    // You can exclude the *.map files from the build during deployment.
    devtool: shouldUseSourceMap ? 'source-map' : false,
    // In production, we only want to load the polyfills and the app code.
    entry: {
        repository: [require.resolve('./polyfills'), paths.resolveApp('src/app-registry/index.js')],
        console: [require.resolve('./polyfills'), paths.resolveApp('src/app-console/index.js')],
        workflow: [require.resolve('./polyfills'), paths.resolveApp('src/app-workflow/index.js')],
        listeners: [require.resolve('./polyfills'), paths.resolveApp('src/app-listeners/index.js')],
        audit: [require.resolve('./polyfills'), paths.resolveApp('src/app-audit/index.js')],
        rest: [require.resolve('./polyfills'), paths.resolveApp('src/app-rest/index.js')],
        cf: [require.resolve('./polyfills'), paths.resolveApp('src/app-cf/index.js')],
        fields: [require.resolve('./polyfills'), paths.resolveApp('src/app-fields/index.js')],
        extras: [require.resolve('./polyfills'), paths.resolveApp('src/app-extras/index.js')],
    },
    output: {
        // The build folder.
        path: paths.appBuild,
        // Generated JS file names (with nested folders).
        // There will be one main bundle, and one file per asynchronous chunk.
        // We don't currently advertise code splitting but Webpack supports it.
        filename: '../../src/main/resources/ru/mail/jira/plugins/groovy/js/[name].js',
        chunkFilename: '../../src/main/resources/ru/mail/jira/plugins/groovy/js/[name].chunk.js',
        // We inferred the "public path" (such as / or /my-project) from homepage.
        publicPath: publicPath,
        // Point sourcemap entries to original disk location (format as URL on Windows)
        devtoolModuleFilenameTemplate: info =>
            path
                .relative(paths.appSrc, info.absoluteResourcePath)
                .replace(/\\/g, '/'),
        jsonpFunction: 'mailruGroovyWebpackJsonp' //override default jsonpFunction to avoid collision with other webpack apps
    },
    resolve: {
        // This allows you to set a fallback for where Webpack should look for modules.
        // We placed these paths second because we want `node_modules` to "win"
        // if there are any conflicts. This matches Node resolution mechanism.
        // https://github.com/facebookincubator/create-react-app/issues/253
        modules: ['node_modules', paths.appNodeModules, paths.resolveApp('../src/main/resources/')].concat(
            // It is guaranteed to exist because we tweak it in `env.js`
            process.env.NODE_PATH.split(path.delimiter).filter(Boolean)
        ),
        // These are the reasonable defaults supported by the Node ecosystem.
        // We also include JSX as a common component filename extension to support
        // some tools, although we do not recommend using it, see:
        // https://github.com/facebookincubator/create-react-app/issues/290
        // `web` extension prefixes have been added for better support
        // for React Native Web.
        extensions: ['.web.js', '.js', '.json', '.web.jsx', '.jsx'],
        alias: {},
        plugins: [
            // Prevents users from importing files from outside of src/ (or node_modules/).
            // This often causes confusion because we only process files within src/ with babel.
            // To fix this, we prevent you from importing files out of src/ -- if you'd like to,
            // please link the files into your node_modules/ and let module-resolution kick in.
            // Make sure your source files are compiled, as they will not be processed in any way.
            new ModuleScopePlugin(paths.appSrc, [paths.appPackageJson]),
        ],
    },
    module: {
        strictExportPresence: true,
        rules: [
            {
                parser: {
                    requireEnsure: false,
                    amd: true
                }
            },

            // First, run the linter.
            // It's important to do this before Babel processes the JS.
            {
                test: /\.(js|jsx)$/,
                enforce: 'pre',
                use: [
                    {
                        options: {
                            formatter: eslintFormatter,
                            eslintPath: require.resolve('eslint'),
                        },
                        loader: require.resolve('eslint-loader'),
                    },
                ],
                include: paths.appSrc,
            },
            {
                // "oneOf" will traverse all following loaders until one will
                // match the requirements. When no loader matches it will fall
                // back to the "file" loader at the end of the loader list.
                oneOf: [
                    // "url" loader works just like "file" loader but it also embeds
                    // assets smaller than specified size as data URLs to avoid requests.
                    {
                        test: [/\.bmp$/, /\.gif$/, /\.jpe?g$/, /\.png$/],
                        loader: require.resolve('url-loader'),
                        options: {
                            limit: 10000,
                            name: 'static/media/[name].[hash:8].[ext]',
                        },
                    },
                    // Process JS with Babel.
                    {
                        test: /\.(js|jsx)$/,
                        include: [paths.appSrc, 'node_modules/'],
                        loader: require.resolve('babel-loader'),
                        options: {
                            compact: true,
                            plugins: [
                                ['transform-decorators-legacy']
                            ]
                        },
                    },
                    {
                        test: /\.css$/,
                        use: [
                            require.resolve('style-loader'),
                            {
                                loader: require.resolve('css-loader'),
                                options: {
                                    importLoaders: 1,
                                },
                            },
                            {
                                loader: require.resolve('postcss-loader'),
                                options: {
                                    // Necessary for external CSS imports to work
                                    // https://github.com/facebookincubator/create-react-app/issues/2677
                                    ident: 'postcss',
                                    plugins: () => [
                                        require('postcss-flexbugs-fixes'),
                                        autoprefixer({
                                            browsers: [
                                                '>1%',
                                                'last 4 versions',
                                                'Firefox ESR',
                                                'not ie < 9', // React doesn't support IE8 anyway
                                            ],
                                            flexbox: 'no-2009',
                                        }),
                                    ],
                                },
                            },
                        ],
                    },
                    {
                        test: /\.less$/,
                        use: [
                            {
                                loader: 'style-loader'
                            },
                            {
                                loader: 'css-loader'
                            },
                            {
                                loader: 'less-loader'
                            }
                        ]
                    },
                    {
                        test: /\.properties/,
                        loader: require.resolve('properties-loader')
                    },

                    // "file" loader makes sure assets end up in the `build` folder.
                    // When you `import` an asset, you get its filename.
                    // This loader doesn't use a "test" so it will catch all modules
                    // that fall through the other loaders.
                    {
                        loader: require.resolve('file-loader'),
                        // Exclude `js` files to keep "css" loader working as it injects
                        // it's runtime that would otherwise processed through "file" loader.
                        // Also exclude `html` and `json` extensions so they get processed
                        // by webpacks internal loaders.
                        exclude: [/\.js$/, /\.html$/, /\.json$/],
                        options: {
                            name: 'static/media/[name].[hash:8].[ext]',
                        },
                    },
                    // ** STOP ** Are you adding a new loader?
                    // Make sure to add the new loader(s) before the "file" loader.
                ],
            },
        ],
    },
    plugins: [
        // Makes some environment variables available to the JS code, for example:
        // if (process.env.NODE_ENV === 'production') { ... }. See `./env.js`.
        // It is absolutely essential that NODE_ENV was set to production here.
        // Otherwise React will be compiled in the very slow development mode.
        new webpack.DefinePlugin(env.stringified),
        // Minify the code.
        ...uglify,
        new webpack.optimize.CommonsChunkPlugin({
            name: 'common',
            minChunks: 2
        }),
        // Note: this won't work without ExtractTextPlugin.extract(..) in `loaders`.
        new ExtractTextPlugin({
            filename: cssFilename,
            allChunks: true
        }),
        new ManifestPlugin({
            fileName: 'asset-manifest.json',
        }),
        // Moment.js is an extremely popular library that bundles large locale files
        // by default due to how Webpack interprets its code. This is a practical
        // solution that requires the user to opt into importing specific locales.
        // https://github.com/jmblog/how-to-optimize-momentjs-with-webpack
        // You can remove this if you don't use Moment.js:
        //todo:
        //new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /ru|en/),
        //new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
        ...bundle
    ],
    // Some libraries import Node modules but don't use them in the browser.
    // Tell Webpack to provide empty mocks for them so importing them works.
    node: {
        dgram: 'empty',
        fs: 'empty',
        net: 'empty',
        tls: 'empty',
        child_process: 'empty',
    },
    externals: {
        AJS: 'AJS',
        jquery: 'AJS.$',
        'external-i18n': 'require(\'/mailru/groovy/i18n-react\')',
        extDefine: 'define'
    },
    watch: watch
};
