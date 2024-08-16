<?php
/*
Plugin Name: Image Compression Pro
Plugin URI: https://yourwebsite.com/image-compression-pro
Description: Compress images uploaded to WordPress using a custom image minify service.
Version: 1.5
Author: Your Name
Author URI: https://yourwebsite.com
*/

// Exit if accessed directly
if (!defined('ABSPATH')) {
    exit;
}

class ImageCompressionPro {
    private $api_url = 'http://185.136.206.146:8085/api/images/upload';
    private $quality = 70;
    private $api_key = '';
    private $exclude_sizes = array();
    private $automatic_compression = false;
    private $backup_original = false;

    public function __construct() {
        add_action('admin_menu', array($this, 'add_plugin_page'));
        add_action('admin_init', array($this, 'register_settings'));
        add_action('admin_enqueue_scripts', array($this, 'enqueue_admin_scripts'));
        add_filter('wp_handle_upload', array($this, 'compress_uploaded_image'));
        add_action('wp_ajax_compress_existing_image', array($this, 'compress_existing_image'));
        add_action('wp_ajax_get_compression_stats', array($this, 'get_compression_stats'));
        add_action('wp_ajax_get_compression_progress', array($this, 'get_compression_progress'));
        add_action('wp_ajax_restore_original_image', array($this, 'restore_original_image'));
        add_action('admin_notices', array($this, 'show_admin_notices'));

        $this->api_key = get_option('image_compression_pro_api_key', '');
        $this->quality = get_option('image_compression_pro_quality', 70);
        $this->exclude_sizes = get_option('image_compression_pro_exclude_sizes', array());
        $this->automatic_compression = get_option('image_compression_pro_automatic_compression', false);
        $this->backup_original = get_option('image_compression_pro_backup_original', false);

        // Start background process for automatic compression
        if ($this->automatic_compression) {
            $this->start_background_compression();
        }
    }

    public function add_plugin_page() {
        add_menu_page(
            'Image Compression Pro',
            'Image Compression',
            'manage_options',
            'image-compression-pro',
            array($this, 'create_admin_page'),
            'dashicons-images-alt2',
            60
        );
    }

    public function register_settings() {
        register_setting('image_compression_pro_options', 'image_compression_pro_api_key');
        register_setting('image_compression_pro_options', 'image_compression_pro_quality', array(
            'type' => 'integer',
            'sanitize_callback' => function($value) {
                return max(1, min(100, intval($value)));
            }
        ));
        register_setting('image_compression_pro_options', 'image_compression_pro_exclude_sizes', array(
            'type' => 'array',
            'sanitize_callback' => function($value) {
                return array_map('sanitize_text_field', $value);
            }
        ));
        register_setting('image_compression_pro_options', 'image_compression_pro_automatic_compression', 'boolval');
        register_setting('image_compression_pro_options', 'image_compression_pro_backup_original', 'boolval');
    }

    public function enqueue_admin_scripts($hook) {
        if ($hook != 'toplevel_page_image-compression-pro') {
            return;
        }
        wp_enqueue_style('image-compression-pro-css', plugin_dir_url(__FILE__) . 'image-compression-pro.css');
        wp_enqueue_script('image-compression-pro-js', plugin_dir_url(__FILE__) . 'image-compression-pro.js', array('jquery', 'jquery-ui-slider'), '1.5', true);
        wp_localize_script('image-compression-pro-js', 'image_compression_pro', array(
            'ajax_url' => admin_url('admin-ajax.php'),
            'nonce' => wp_create_nonce('image_compression_pro_nonce')
        ));
    }

    public function create_admin_page() {
        ?>
        <div class="wrap">
            <h1>Image Compression Pro</h1>

            <div class="notice notice-info">
                <p><strong>API Key:</strong> You can obtain an API key from <a href="https://yourwebsite.com/api-keys" target="_blank">yourwebsite.com/api-keys</a></p>
            </div>

            <form method="post" action="options.php">
                <?php
                settings_fields('image_compression_pro_options');
                do_settings_sections('image_compression_pro_options');
                ?>
                <table class="form-table">
                    <tr valign="top">
                        <th scope="row">API Key</th>
                        <td><input type="text" name="image_compression_pro_api_key" value="<?php echo esc_attr($this->api_key); ?>" /></td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Image Quality</th>
                        <td>
                            <div id="quality-slider"></div>
                            <input type="hidden" name="image_compression_pro_quality" id="quality-input" value="<?php echo esc_attr($this->quality); ?>" />
                            <p class="description">Slide to adjust image quality (1-100). Current value: <span id="quality-value"><?php echo esc_html($this->quality); ?></span></p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Exclude Sizes</th>
                        <td>
                            <?php
                            $sizes = get_intermediate_image_sizes();
                            foreach ($sizes as $size) {
                                $checked = in_array($size, $this->exclude_sizes) ? 'checked' : '';
                                echo '<input type="checkbox" name="image_compression_pro_exclude_sizes[]" value="' . $size . '" ' . $checked . ' /> ' . $size . '<br>';
                            }
                            ?>
                            <p class="description">Select image sizes to exclude from compression.</p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Automatic Compression</th>
                        <td>
                            <input type="checkbox" name="image_compression_pro_automatic_compression" <?php checked($this->automatic_compression); ?> />
                            <p class="description">Enable automatic compression of newly uploaded images.</p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Backup Original Images</th>
                        <td>
                            <input type="checkbox" name="image_compression_pro_backup_original" <?php checked($this->backup_original); ?> />
                            <p class="description">Create a backup of original images before compression.</p>
                        </td>
                    </tr>
                </table>
                <?php submit_button(); ?>
            </form>

            <div class="image-compression-pro-container">
                <div class="image-compression-pro-stats">
                    <h2>Compression Statistics</h2>
                    <div id="compression-stats-container">
                        <!-- Stats will be populated via AJAX -->
                    </div>
                </div>

                <div class="image-compression-pro-compress">
                    <h2>Compress Existing Images</h2>
                    <p>Select an image from the dropdown and click "Compress Image".</p>
                    <select id="image-select">
                        <?php
                        $attachments = get_posts(array(
                            'post_type' => 'attachment',
                            'post_mime_type' => 'image',
                            'posts_per_page' => -1,
                        ));
                        foreach ($attachments as $attachment) {
                            $original_url = wp_get_attachment_url($attachment->ID);
                            echo '<option value="' . $attachment->ID . '" data-original-url="' . $original_url . '">' . $attachment->post_title . '</option>';
                        }
                        ?>
                    </select>
                    <button id="compress-image-button" class="button button-primary">Compress Image</button>
                    <div id="compression-progress"></div>
                    <div id="compression-result"></div>
                </div>
            </div>
        </div>
        <?php
    }

    public function compress_uploaded_image($upload) {
        if (strpos($upload['type'], 'image') === false) {
            return $upload;
        }

        if ($this->automatic_compression) {
            $this->compress_image_async($upload['file']);
        }

        return $upload;
    }

    private function compress_image_async($file_path) {
        $args = array(
            'file_path' => $file_path,
        );
        wp_schedule_single_event(time(), 'image_compression_pro_compress_image', $args);
    }

    public function compress_existing_image() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachment_id = intval($_POST['attachment_id']);
        $file_path = get_attached_file($attachment_id);

        // Start progress tracking
        $this->update_compression_progress(0, $attachment_id);

        $compressed_image = $this->compress_image($file_path);

        if ($compressed_image) {
            file_put_contents($file_path, base64_decode($compressed_image['compressedImage']));
            $this->update_compression_stats($compressed_image);

            // Finish progress tracking
            $this->update_compression_progress(100, $attachment_id);

            // Update image metadata
            $this->update_image_metadata($file_path, $compressed_image);

            // Optionally create a backup
            if ($this->backup_original) {
                $backup_path = $this->get_backup_path($attachment_id);
                copy($file_path, $backup_path);
            }

            wp_send_json_success($compressed_image);
        } else {
            wp_send_json_error('Compression failed');
        }
    }

    public function get_compression_stats() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $stats = get_option('image_compression_pro_stats', array(
            'total_compressions' => 0,
            'total_saved_bytes' => 0,
            'average_compression_ratio' => 0,
        ));

        wp_send_json_success($stats);
    }

    public function get_compression_progress() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachment_id = intval($_POST['attachment_id']);
        $progress_data = get_option('image_compression_pro_progress', array());
        $progress = isset($progress_data[$attachment_id]) ? $progress_data[$attachment_id] : 0;

        wp_send_json_success($progress);
    }

    private function compress_image($file_path) {
        if (!function_exists('curl_init')) {
            return false;
        }

        $curl = curl_init();

        curl_setopt_array($curl, array(
            CURLOPT_URL => $this->api_url . '?quality=' . $this->quality,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => '',
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 0,
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => 'POST',
            CURLOPT_POSTFIELDS => array(
                'file' => new CURLFILE($file_path)
            ),
            CURLOPT_HTTPHEADER => array(
                'Accept: */*',
                'API-KEY: ' . $this->api_key
            ),
        ));

        $response = curl_exec($curl);
        curl_close($curl);

        return json_decode($response, true);
    }

    private function update_compression_stats($compressed_image) {
        $stats = get_option('image_compression_pro_stats', array(
            'total_compressions' => 0,
            'total_saved_bytes' => 0,
            'average_compression_ratio' => 0,
        ));

        $stats['total_compressions']++;
        $stats['total_saved_bytes'] += ($compressed_image['originalSize'] - $compressed_image['compressedSize']);
        $stats['average_compression_ratio'] = (($stats['average_compression_ratio'] * ($stats['total_compressions'] - 1)) + $compressed_image['compressionRatio']) / $stats['total_compressions'];

        update_option('image_compression_pro_stats', $stats);
    }

    private function update_compression_progress($progress, $attachment_id) {
        $progress_data = get_option('image_compression_pro_progress', array());
        $progress_data[$attachment_id] = $progress;
        update_option('image_compression_pro_progress', $progress_data);
    }

    public function restore_original_image() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachment_id = intval($_POST['attachment_id']);
        $backup_path = $this->get_backup_path($attachment_id);
        $original_path = get_attached_file($attachment_id);

        if (file_exists($backup_path)) {
            if (copy($backup_path, $original_path)) {
                wp_send_json_success('Original image restored.');
            } else {
                wp_send_json_error('Failed to restore original image.');
            }
        } else {
            wp_send_json_error('Backup file not found.');
        }
    }

    private function get_backup_path($attachment_id) {
        $file_path = get_attached_file($attachment_id);
        return $file_path . '.bak';
    }

    private function start_background_compression() {
        add_action('image_compression_pro_compress_image', array($this, 'background_compress_image'));
    }

    public function background_compress_image($args) {
        $file_path = $args['file_path'];

        // Check if the file is an image and not excluded
        $image_metadata = wp_get_attachment_metadata(attachment_url_to_postid($file_path));
        if (!$image_metadata || empty($image_metadata['file'])) {
            return;
        }

        $file_size = filesize($file_path);
        if ($this->should_exclude_image($file_size, $image_metadata)) {
            return;
        }

        // Compress the image
        $compressed_image = $this->compress_image($file_path);

        if ($compressed_image) {
            // Update image metadata
            $this->update_image_metadata($file_path, $compressed_image);

            // Optionally create a backup
            if ($this->backup_original) {
                $backup_path = $this->get_backup_path(attachment_url_to_postid($file_path));
                copy($file_path, $backup_path);
            }
        }
    }

    private function should_exclude_image($file_size, $image_metadata) {
        // Check if the image size is less than the exclusion limit
        $exclude_size_limit = get_option('image_compression_pro_exclude_size_limit', 0); // In bytes
        if ($file_size < $exclude_size_limit) {
            return true;
        }

        // Check if the image size is in the excluded sizes
        foreach ($this->exclude_sizes as $size) {
            if (isset($image_metadata['sizes'][$size])) {
                return true;
            }
        }

        return false;
    }

    private function update_image_metadata($file_path, $compressed_image) {
        $attachment_id = attachment_url_to_postid($file_path);
        $image_metadata = wp_get_attachment_metadata($attachment_id);

        // Update file size in metadata
        $image_metadata['filesize'] = $compressed_image['compressedSize'];

        // Update sizes metadata (if applicable)
        if (isset($image_metadata['sizes'])) {
            foreach ($image_metadata['sizes'] as $size => $data) {
                $size_file_path = path_join(dirname($file_path), $data['file']);
                if (file_exists($size_file_path)) {
                    $image_metadata['sizes'][$size]['filesize'] = filesize($size_file_path);
                }
            }
        }

        wp_update_attachment_metadata($attachment_id, $image_metadata);
    }

    public function show_admin_notices() {
        if (isset($_GET['image_compression_pro_notice'])) {
            $notice = sanitize_text_field($_GET['image_compression_pro_notice']);
            echo '<div class="notice notice-success is-dismissible"><p>' . $notice . '</p></div>';
        }
    }
}

$image_compression_pro = new ImageCompressionPro();

// Add CSS
function image_compression_pro_css() {
    ?>
    <style>
    .image-compression-pro-container {
        display: flex;
        justify-content: space-between;
        margin-top: 20px;
    }
    .image-compression-pro-stats,
    .image-compression-pro-compress {
        width: 48%;
        background: #fff;
        padding: 20px;
        border-radius: 5px;
        box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }
    #compression-stats-container,
    #compression-result {
        margin-top: 20px;
    }
    #image-select {
        width: 100%;
        margin-bottom: 10px;
    }
    #compress-image-button {
        width: 100%;
    }
    #quality-slider {
        width: 300px;
        margin: 10px 0;
    }
    #compression-progress {
        margin-top: 10px;
    }
    </style>
    <?php
}
add_action('admin_head', 'image_compression_pro_css');

// Add JavaScript
function image_compression_pro_js() {
    ?>
    <script>
    jQuery(document).ready(function($) {
        $("#quality-slider").slider({
            min: 1,
            max: 100,
            value: <?php echo get_option('image_compression_pro_quality', 70); ?>,
            slide: function(event, ui) {
                $("#quality-input").val(ui.value);
                $("#quality-value").text(ui.value);
            }
        });

        function loadCompressionStats() {
            $.ajax({
                url: image_compression_pro.ajax_url,
                type: 'POST',
                data: {
                    action: 'get_compression_stats',
                    nonce: image_compression_pro.nonce
                },
                success: function(response) {
                    if (response.success) {
                        var stats = response.data;
                        var statsHtml = '<p>Total Compressions: ' + stats.total_compressions + '</p>' +
                                        '<p>Total Saved: ' + formatBytes(stats.total_saved_bytes) + '</p>' +
                                        '<p>Average Compression Ratio: ' + stats.average_compression_ratio.toFixed(2) + '%</p>';
                        $('#compression-stats-container').html(statsHtml);
                    }
                }
            });
        }

        function formatBytes(bytes, decimals = 2) {
            if (bytes === 0) return '0 Bytes';
            const k = 1024;
            const dm = decimals < 0 ? 0 : decimals;
            const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
        }

        $('#compress-image-button').on('click', function() {
            var attachmentId = $('#image-select').val();
            $.ajax({
                url: image_compression_pro.ajax_url,
                type: 'POST',
                data: {
                    action: 'compress_existing_image',
                    nonce: image_compression_pro.nonce,
                    attachment_id: attachmentId
                },
                beforeSend: function() {
                    $('#compression-result').html('<p>Compressing... Please wait.</p>');
                    $('#compression-progress').html('<progress value="0" max="100"></progress>');
                },
                success: function(response) {
                    if (response.success) {
                        var result = response.data;
                        var resultHtml = '<p>Original Size: ' + formatBytes(result.originalSize) + '</p>' +
                                         '<p>Compressed Size: ' + formatBytes(result.compressedSize) + '</p>' +
                                         '<p>Compression Ratio: ' + result.compressionRatio.toFixed(2) + '%</p>';
                        $('#compression-result').html(resultHtml);
                        loadCompressionStats();

                        // Add visual comparison
                        var originalImageUrl = $('#image-select option:selected').data('original-url');
                        var compressedImageUrl = wp_get_attachment_url(attachmentId);

                        if (originalImageUrl) {
                            $('#compression-result').append('<p><strong>Visual Comparison:</strong></p>');
                            $('#compression-result').append('<img src="' + originalImageUrl + '" style="max-width: 300px; margin-right: 10px;" />');
                            $('#compression-result').append('<img src="' + compressedImageUrl + '" style="max-width: 300px;" />');
                        }
                    } else {
                        $('#compression-result').html('<p>Compression failed. Please check your API key and try again.</p>');
                    }
                },
                error: function() {
                    $('#compression-result').html('<p>An error occurred. Please try again later.</p>');
                },
                complete: function() {
                    // Hide the progress bar after completion (success or error)
                    $('#compression-progress').html('');
                }
            });

            // Update progress bar
            var progressInterval = setInterval(function() {
                $.ajax({
                    url: image_compression_pro.ajax_url,
                    type: 'POST',
                    data: {
                        action: 'get_compression_progress',
                        nonce: image_compression_pro.nonce,
                        attachment_id: attachmentId
                    },
                    success: function(response) {
                        if (response.success) {
                            var progress = response.data;
                            $('#compression-progress progress').val(progress);
                        }
                    }
                });
            }, 1000);
        });

        loadCompressionStats();
    });
    </script>
    <?php
}
add_action('admin_footer', 'image_compression_pro_js');

// Add background process function
add_action('image_compression_pro_compress_image', array($image_compression_pro, 'background_compress_image'));