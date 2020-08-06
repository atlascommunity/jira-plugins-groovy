const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const WrmPlugin = require('atlassian-webresource-webpack-plugin');
const TerserPlugin = require("terser-webpack-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

const devMode = process.env.NODE_ENV !== 'production';
const OUTPUT_DIR = '../src/main/resources/';

module.exports = {
    entry: {
        main: ['./config/polyfills', './src/app-main/index.js'],
        workflow: ['./config/polyfills', './src/app-workflow/index.js'],
    },
    output: {
        path: path.resolve(OUTPUT_DIR),
        filename: 'ru/mail/jira/plugins/groovy/js/[name].js',
        chunkFilename: 'ru/mail/jira/plugins/groovy/js/[name].chunk.js',
        publicPath: "/assets/",
        jsonpFunction: 'mailruGroovyWebpackJsonp',
        sourceMapFilename: '[file].smap'
    },
    optimization: {
        minimizer: [
            new TerserPlugin({
                cache: true,
                parallel: true,
                sourceMap: true
            }),
            new OptimizeCSSAssetsPlugin({})
        ],
        splitChunks: {
            cacheGroups: {
                commons: {
                    name: 'commons',
                    chunks: 'initial',
                    minChunks: 2
                }
            }
        }
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: "ru/mail/jira/plugins/groovy/css/[name].css",
            chunkFilename: "ru/mail/jira/plugins/groovy/css/[name].chunk.css"
        }),
        new WrmPlugin({
            pluginKey: 'ru.mail.jira.plugins.groovy',
            amdProvider: 'jira.webresources:almond',
            xmlDescriptors: path.resolve(OUTPUT_DIR, 'META-INF', 'plugin-descriptors', 'wr-defs.xml'),
            contextMap: {
                main: ['ru.mail.jira.plugins.groovy.main'],
                workflow: ['ru.mail.jira.plugins.groovy.workflow'],
            },
            providedDependencies: {
                'jquery': {
                    dependency: 'com.atlassian.plugins.jquery:jquery',
                    import: {
                        var: 'AJS.$'
                    }
                },
                'AJS': {
                    dependency: 'com.atlassian.auiplugin:aui-core',
                    import: {
                        var: 'AJS'
                    }
                },
                'external-i18n': {
                    dependency: 'ru.mail.jira.plugins.groovy:react-i18n',
                    import: {
                        var: 'require(\'mailru/groovy/i18n-react\')'
                    }
                }
            }
        })
    ],
    externals: {
        extDefine: 'define',
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                enforce: 'pre',
                loader: 'eslint-loader'
            },
            {
                oneOf: [
                    {
                        test: /\.js$/,
                        exclude: /node_modules/,
                        loader: 'babel-loader',
                        options: {
                            presets: [
                                [
                                    require('@babel/preset-env'),
                                    {
                                        //based on https://confluence.atlassian.com/adminjiraserver073/supported-platforms-861253018.html
                                        targets: {
                                            chrome: "69",
                                            ie: "11",
                                            edge: "42",
                                            firefox: "62",
                                            safari: "12"
                                        },
                                        ignoreBrowserslistConfig: true,
                                        useBuiltIns: false,
                                        modules: false,
                                        exclude: ['transform-typeof-symbol'],
                                    },
                                ],
                                [
                                    require('@babel/preset-react'),
                                    {
                                        useBuiltIns: true,
                                    }
                                ]
                            ],
                            plugins: [
                                //'flow-react-proptypes',
                                require('@babel/plugin-transform-flow-strip-types'),
                                require('@babel/plugin-transform-destructuring'),
                                [
                                    require('@babel/plugin-proposal-class-properties'),
                                    {
                                        loose: true,
                                    }
                                ],
                                [
                                    require('@babel/plugin-proposal-object-rest-spread'),
                                    {
                                        useBuiltIns: true,
                                    }
                                ],
                                !devMode && [
                                    // Remove PropTypes from production build
                                    require('babel-plugin-transform-react-remove-prop-types').default,
                                    {
                                        removeImport: true,
                                    },
                                ],
                            ].filter(it => it)
                        }
                    },
                    {
                        test: /\.less$/,
                        use: [
                            devMode ? 'style-loader' : MiniCssExtractPlugin.loader,
                            'css-loader',
                            {
                                loader: 'less-loader',
                                options: {
                                    noIeCompat: false
                                }
                            }
                        ]
                    },
                    {
                        loader: require.resolve('file-loader'),
                        exclude: [/\.js$/, /\.html$/, /\.json$/],
                        options: {
                            name: 'static/media/[name].[hash:8].[ext]',
                        },
                    },
                ]
            }
        ]
    }
};
